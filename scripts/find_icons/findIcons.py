import os

def find_dds_folders(root_folder, output_file):
    # 打开文件写入
    with open(output_file, 'w') as f:
        # 遍历根文件夹下的所有子文件夹及文件
        for root, dirs, files in os.walk(root_folder):
            # 如果当前目录下有.dss文件
            if any(file.lower().endswith('.dds') for file in files):
                # "\"替换为"/"
                root = root.replace('\\', '/')
                
                # 记录该目录路径，并加上双引号和逗号
                f.write(f'"{root}",\n')

def find_png_folders(root_folder, output_file):
    # 打开文件写入
    with open(output_file, 'w') as f:
        # 遍历根文件夹下的所有子文件夹及文件
        for root, dirs, files in os.walk(root_folder):
            # 如果当前目录下有.dss文件
            if any(file.lower().endswith('.png') for file in files):
                # "\"替换为"/"
                root = root.replace('\\', '/')
                
                # 记录该目录路径，并加上双引号和逗号
                f.write(f'"{root}",\n')

root_folder = 'E:/SteamLibrary/steamapps/workshop/content/289070'
# root_folder = "<Documents>/My Games/Sid Meier's Civilization VI/Mods"
output_file_dds = './find_icons/found_dds_folders.txt'
output_file_png = './find_icons/found_png_folders.txt'

# find_dds_folders(root_folder, output_file_dds)
find_png_folders(root_folder, output_file_png)
