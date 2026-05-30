import sqlite3

def delete_recursively_by_column(conn, table, column, value, exempt_tables=None):
    """
    从指定表中某一列的值递归删除所有关联的数据，包括父表和子表。
    
    :param conn: sqlite3 数据库连接
    :param table: 起始表名
    :param column: 起始列名
    :param value: 起始列的值
    :param exempt_tables: 豁免表名列表，若当前表在其中，将跳过该表的处理。
    """
    if exempt_tables is None:
        exempt_tables = []  # 默认无豁免表

    visited = set()  # 记录已处理的表和行，避免重复删除

    def fetch_foreign_keys(table_name):
        """获取表的外键关系（子表引用当前表）。"""
        cursor = conn.execute(f"PRAGMA foreign_key_list({table_name})")
        foreign_keys = []
        for row in cursor.fetchall():
            foreign_keys.append({
                'table': row[2],  # 外键关联的表（子表）
                'from': row[3],  # 当前表的字段
                'to': row[4],  # 关联表的字段
                'on_delete': row[5]  # 删除操作的行为
            })
        return foreign_keys

    def fetch_reverse_foreign_keys(table_name):
        """获取反向外键关系（当前表引用父表）。"""
        reverse_foreign_keys = []
        tables = conn.execute("SELECT name FROM sqlite_master WHERE type='table';").fetchall()
        for t in tables:
            t_name = t[0]
            fk_list = fetch_foreign_keys(t_name)
            for fk in fk_list:
                if fk['table'] == table_name:  # 找到引用当前表的外键
                    reverse_foreign_keys.append({
                        'table': t_name,  # 父表
                        'from': fk['to'],  # 当前表的字段（外键）
                        'to': fk['from']  # 父表的字段（主键）
                    })
        return reverse_foreign_keys

    def get_primary_key(table_name):
        """获取表的主键列名。"""
        cursor = conn.execute(f"PRAGMA table_info({table_name})")
        for row in cursor.fetchall():
            if row[5]:  # 主键列标记
                return row[1]
        return "rowid"  # 默认使用 rowid 作为主键

    def recursive_delete(current_table, current_column, current_value):
        """递归删除指定表和行的所有关联数据。"""
        # 如果当前表在豁免列表中，跳过处理
        if current_table in exempt_tables:
            print(f"Skipping exempted table '{current_table}'")
            return

        primary_key = get_primary_key(current_table)
        
        if (current_table, current_column, current_value) in visited:
            print(f"Already visited: {current_table}({current_column}={current_value})")
            return  # 避免重复处理
        visited.add((current_table, current_column, current_value))

        print(f"Deleting in table '{current_table}' where {current_column} = {current_value}")

        # 获取当前行的主键值
        cursor = conn.execute(
            f"SELECT {primary_key} FROM {current_table} WHERE {current_column} = ?",
            (current_value,)
        )
        rows = cursor.fetchall()
        if not rows:
            print(f"No matching rows found in table '{current_table}' for {current_column} = {current_value}")
            return  # 没有找到匹配的行
        primary_values = [row[0] for row in rows]
        print(f"Primary keys to delete in '{current_table}': {primary_values}")

        # Step 1: 向下删除（子表）
        foreign_keys = fetch_foreign_keys(current_table)
        for pk_value in primary_values:
            for fk in foreign_keys:
                child_table = fk['table']
                child_column = fk['to']
                parent_column = fk['from']

                print(f"Checking child table '{child_table}' where {child_column} references '{current_table}({parent_column})'")
                cursor = conn.execute(
                    f"SELECT {get_primary_key(child_table)} FROM {child_table} WHERE {child_column} = ?",
                    (pk_value,)
                )
                child_ids = [row[0] for row in cursor.fetchall()]
                print(f"Child keys in '{child_table}' to delete: {child_ids}")

                for child_id in child_ids:
                    recursive_delete(child_table, get_primary_key(child_table), child_id)

            # 删除当前行
            print(f"Deleting row in '{current_table}' with primary key = {pk_value}")
            conn.execute(f"DELETE FROM {current_table} WHERE {primary_key} = ?", (pk_value,))

        # Step 2: 向上删除（父表）
        reverse_foreign_keys = fetch_reverse_foreign_keys(current_table)
        for pk_value in primary_values:
            for fk in reverse_foreign_keys:
                parent_table = fk['table']
                parent_column = fk['to']
                child_column = fk['from']

                print(f"Checking parent table '{parent_table}' where {parent_column} is referenced by '{current_table}({child_column})'")
                cursor = conn.execute(
                    f"SELECT {get_primary_key(parent_table)} FROM {parent_table} WHERE {parent_column} = ?",
                    (pk_value,)
                )
                parent_ids = [row[0] for row in cursor.fetchall()]
                print(f"Parent keys in '{parent_table}' to delete: {parent_ids}")

                for parent_id in parent_ids:
                    recursive_delete(parent_table, get_primary_key(parent_table), parent_id)

    # 开始递归删除
    with conn:
        print(f"Starting deletion from table '{table}' where {column} = {value}")
        recursive_delete(table, column, value)
        print("Deletion complete.")


def generate_constraint_graph(conn, table, column, value, exempt_tables=None):
    """
    从指定表的某列值开始，生成与该行相关的约束关系图，并以文字格式打印。
    
    :param conn: sqlite3 数据库连接
    :param table: 起始表名
    :param column: 起始列名
    :param value: 起始列的值
    :param exempt_tables: 豁免表名列表，若当前表在其中，将跳过该表的处理。
    """
    if exempt_tables is None:
        exempt_tables = []  # 默认无豁免表
    visited = set()  # 防止循环依赖的记录
    graph = []  # 用于存储约束图信息

    def fetch_foreign_keys(table_name):
        """获取表的外键关系（子表引用当前表）。"""
        cursor = conn.execute(f"PRAGMA foreign_key_list({table_name})")
        foreign_keys = []
        for row in cursor.fetchall():
            foreign_keys.append({
                'table': row[2],  # 外键关联的表（子表）
                'from': row[3],  # 当前表的字段
                'to': row[4],  # 关联表的字段
                'on_delete': row[5]  # 删除操作的行为
            })
        return foreign_keys

    def fetch_reverse_foreign_keys(table_name):
        """获取反向外键关系（当前表引用父表）。"""
        reverse_foreign_keys = []
        tables = conn.execute("SELECT name FROM sqlite_master WHERE type='table';").fetchall()
        for t in tables:
            t_name = t[0]
            fk_list = fetch_foreign_keys(t_name)
            for fk in fk_list:
                if fk['table'] == table_name:  # 找到引用当前表的外键
                    reverse_foreign_keys.append({
                        'table': t_name,  # 父表
                        'from': fk['to'],  # 当前表的字段（外键）
                        'to': fk['from']  # 父表的字段（主键）
                    })
        return reverse_foreign_keys

    def get_primary_key(table_name):
        """获取表的主键列名。"""
        cursor = conn.execute(f"PRAGMA table_info({table_name})")
        for row in cursor.fetchall():
            if row[5]:  # 主键列标记
                return row[1]
        return "rowid"  # 默认使用 rowid 作为主键

    def traverse_constraints(current_table, current_column, current_value, depth=0):
        """递归遍历约束关系图。"""
        # 如果当前表在豁免列表中，跳过处理
        if current_table in exempt_tables:
            graph.append(f"{'  ' * depth}Exempted: {current_table} (skipped)")
            return

        primary_key = get_primary_key(current_table)
        
        # 检查是否已经访问过
        if (current_table, current_column, current_value) in visited:
            return
        visited.add((current_table, current_column, current_value))

        # 添加当前行到图
        graph.append(f"{'  ' * depth}{current_table}({current_column}={current_value})")

        # 获取当前行的主键值
        cursor = conn.execute(
            f"SELECT {primary_key} FROM {current_table} WHERE {current_column} = ?",
            (current_value,)
        )
        rows = cursor.fetchall()
        if not rows:
            return  # 没有找到匹配的行
        primary_values = [row[0] for row in rows]

        # 遍历子表
        foreign_keys = fetch_foreign_keys(current_table)
        for pk_value in primary_values:
            for fk in foreign_keys:
                child_table = fk['table']
                child_column = fk['to']
                parent_column = fk['from']

                cursor = conn.execute(
                    f"SELECT {get_primary_key(child_table)} FROM {child_table} WHERE {child_column} = ?",
                    (pk_value,)
                )
                child_ids = [row[0] for row in cursor.fetchall()]

                for child_id in child_ids:
                    graph.append(f"{'  ' * (depth + 1)}Child: {child_table}({child_column}={child_id})")
                    traverse_constraints(child_table, get_primary_key(child_table), child_id, depth + 2)

        # 遍历父表
        reverse_foreign_keys = fetch_reverse_foreign_keys(current_table)
        for pk_value in primary_values:
            for fk in reverse_foreign_keys:
                parent_table = fk['table']
                parent_column = fk['to']
                child_column = fk['from']

                cursor = conn.execute(
                    f"SELECT {get_primary_key(parent_table)} FROM {parent_table} WHERE {parent_column} = ?",
                    (pk_value,)
                )
                parent_ids = [row[0] for row in cursor.fetchall()]

                for parent_id in parent_ids:
                    graph.append(f"{'  ' * (depth + 1)}Parent: {parent_table}({parent_column}={parent_id})")
                    traverse_constraints(parent_table, get_primary_key(parent_table), parent_id, depth + 2)

    # 开始遍历
    print(f"Generating constraint graph starting from {table}({column}={value})")
    traverse_constraints(table, column, value)

    # 打印约束图
    print("\nConstraint Graph:")
    print("\n".join(graph))

import sqlite3

def replace_value_in_all_tables(conn, old_value, new_value):
    """
    遍历所有表和字段，将值中包含 old_value 的字段替换为 new_value。
    
    :param conn: SQLite 数据库连接
    :param old_value: 要查找的旧值
    :param new_value: 替换的新值
    """
    cursor = conn.cursor()

    # 获取所有表
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [row[0] for row in cursor.fetchall()]

    for table in tables:
        # 获取表的所有列
        cursor.execute(f"PRAGMA table_info({table});")
        columns = [row[1] for row in cursor.fetchall()]

        for column in columns:
            try:
                # 检查列中是否包含目标值
                query = f"SELECT rowid, {column} FROM {table} WHERE {column} LIKE ?"
                cursor.execute(query, (f"%{old_value}%",))
                rows = cursor.fetchall()

                if rows:
                    print(f"Found {old_value} in table '{table}', column '{column}': {len(rows)} row(s)")

                # 替换目标值
                for row in rows:
                    rowid, original_value = row
                    new_column_value = original_value.replace(old_value, new_value)
                    update_query = f"UPDATE {table} SET {column} = ? WHERE rowid = ?"
                    cursor.execute(update_query, (new_column_value, rowid))
                    print(f"Updated table '{table}', column '{column}', rowid {rowid}")
            
            except sqlite3.OperationalError as e:
                # 忽略无法处理的列（如非文本列或复杂查询失败）
                print(f"Skipped column '{column}' in table '{table}' due to error: {e}")

    conn.commit()
    print("All replacements completed.")


if __name__ == "__main__":
    sqlPath = "./database/DebugGameplay.sqlite"

    # ludwig delete
    # tarTable = "Leaders"
    # tarColumn = "LeaderType"
    # tarValue = "LEADER_LUDWIG"
    # exempt_tables = ["Agendas"]

    # yellow_crane delete(preserve yellow_crane_HD)
    # tarTable = "Buildings"
    # tarColumn = "BuildingType"
    # tarValue = "BUILDING_YELLOW_CRANE"
    # exempt_tables = []

    # preslav
    tarTable = "Civilizations"
    tarColumn = "CivilizationType"
    tarValue = "CIVILIZATION_LJUBLJANA"
    exempt_tables = []
    old_value = "LJUBLJANA"
    new_value = "BULGARIA_CS"

    conn = sqlite3.connect(sqlPath)
    # search and print gonstraint graph
    generate_constraint_graph(conn, tarTable, tarColumn, tarValue, exempt_tables)

    # delete
    # delete_recursively_by_column(conn, tarTable, tarColumn, tarValue, exempt_tables)
    
    # replace values
    replace_value_in_all_tables(conn, old_value, new_value)

    conn.close()


