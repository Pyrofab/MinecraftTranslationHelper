# MinecraftTranslationHelper
A tool to aid in the creation of translation files

Mod translations are annoying. You need to fiddle with text files and switch back and forth in your editors to make sure you get it right.
And then the mod updates, new translations are added and you are lost among invalid translations and untranslated features.
If you are a modder, you may also be afraid of changing translation keys as that may break existing translations.

This tool simplifies your work by providing an intuitive interface to work with.

![screenshot](https://image.prntscr.com/image/2PnZaOKYRrWxAJmT_Z0WBA.png)

## Usage instructions
* Download MinecraftTranslationHelper on the [release page](https://github.com/Pyrofab/MinecraftTranslationHelper/releases). Preferably put it near your translations.

* Run it and use the **File | Open** menu item to navigate to the lang folder of the mod you're currently translating.

* After selecting a lang folder, a popup will appear. You can just press `OK` if you want to edit all the existing translation files or you can deselect files you do not want to see and lock files you do not want to edit.  
  <img src="https://image.prntscr.com/image/brgM0B2yTbGHRqI2m2RMmw.png" alt="popup screenshot" height="225" width="125"/>

* The tool wil then load all selected translations side by side with the key on the left side. You can edit any field and the program will do the rest.

* Yes you can drag and drop columns or sort them differently.

* Do you want to delete or change a translation key ? Right click in the table to open the context menu.

* When you are done, click the save button to apply the changes. This will only affect changed files.
  You also have the option to save automatically every few seconds in **File | Toggle autosave**

*Note : this program does not care about comments and fancy line separators. 
Upon hitting save, all irrelevant information will be erased from any edited file.*

### Ask Google
If you run out of inspiration, you can use the 'Ask Google' menu option. 
This will autocomplete the cell based on Google Translate's answer. 
If you use this feature, please double check the result and keep the wacky translations at a minimum.

### Smart Replace
Smart Replace is a feature that lets you translate repetitive lines in a few clicks. 
To open the smart replace dialog, select **Edit | Smart Replace** or press `Ctrl+R`.

![Smart replace dialog screenshot](https://image.prntscr.com/image/bohQv8RyR2mXtB385DC4qw.png)

This will take all lines in the English language file ending with *Item Frame*  and set their French localization to *Cadre* followed by the beginning of the English line. If you want full regex patterns, surround your regular expression with `/` (e.g. the wildcard expression above is equivalent to `/(.*) Item Frame/`). If you just want to use raw strings, uncheck the *Allow wildcards / raw regex* option. The input and output languages can be the same, allowing efficient localization workflow.