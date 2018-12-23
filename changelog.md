# v2.0.0 - The reasonable code update

- Rewrote everything with TornadoFX and fewer disgusting hacks
- Now supports 1.13+ json lang format !
- Added the ability to export lang files to either format
- Added a toggle to (un)lock files once loaded
- Added an autosave option (persistent between launches)
- Added the ability to specify a lang folder to open with a program arg
- Added *open all/none* and *(un)lock all* checkboxes to the file selection dialogue
- Added an **Insert row** option to the context menu
- Added an application icon (thanks to RiverLeviathan)
- Added a `*` indicator to unsaved lang files
- Added a **Help | README** button linking to the repo's readme
- The search replace feature now uses wildcards by default
- Moved every button to a menu bar
- Added a menu item for every feature

# v1.3.0 - The convenience update

- Adds regex search-replace ! Press Ctrl+R, choose your input file, your output file, a regular expression and a string to replace it with and the program will handle everything else !
- Adds change history ! You can now undo your mistakes with Ctrl+Z and redo things with either Ctrl+Shift+Z or Ctrl+Y. (some operations like search-replace will need several undo to cancel completely)
- Adds copy and pasting for entire cells. Select a cell and use Ctrl+C to copy its content, select one or more cell and use Ctrl+V to paste your clipboard's content into the selected cells.
- Now automatically switches to edit mode as soon as you start typing in an editable cell.
- You can now delete the content of a cell by using the del key.

# v1.2.0 - The practical update

- Adds a popup screen when loading a lang folder that allows you to select which lang files to load and which files to lock
- Locked lang files will never be updated by the tool (until reloading)
- Added utf-8 support

# v1.1.1

- Made the sorting operation mark language files as unsaved

# v1.1 - The Shitty Translations update

- Added google translate support (plz dont spam it)
- Added ctrl-s shortcut
- Added confirmation request when closing the program with unsaved changes

# v1.0 - First release
- Idk, it works I guess ?