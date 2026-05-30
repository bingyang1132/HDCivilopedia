## Catalogue of `src/main,java`
- `load`: load info from game databases
- `model`: ?
- `tools`: like utils
- `view`: generate html
    - `DrawLine.java`: layout and style
    - `Page.java`: generate each page


## in /load/Init.java
- why commented some of seem critical loadAsData lines in `Init.java`?

- What are these files for?
    Statement gameplayStatement = DriverManager.getConnection(Tools.GAMEPLAY_DATABASE).createStatement();
    Statement extraStatement = DriverManager.getConnection(Tools.EXTRA_DATABASE).createStatement();
    Statement textStatement = DriverManager.getConnection(Tools.TEXT_DATABASE).createStatement();
    Statement nohdTextStatement = DriverManager.getConnection(Tools.NOHD_TEXT_DATABASE).createStatement();

- Where are the `.jpg` files of icons?