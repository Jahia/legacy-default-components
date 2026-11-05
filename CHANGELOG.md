# legacy-default-components Changelog

## 0.0.1

* Fixed the page creation form component so that a button label or style containing special characters such as `&` or quotes displays as typed.

* Fixed the display of site links whose style values contain special characters, so these values render as typed.

* Fixed the table view of folders for files whose names contain special characters, such as `&`.

* Consistently escape special characters in the user and alias names shown by the login form and shortcuts views, so a name containing characters such as `<`, `>` or `&` renders as written.

* Fixed the sibling navigation component so that page titles containing characters such as `&` or quotes display as written in its link tooltips.

* Fixed the "Back to parent page" component so that its title displays exactly as typed in the link.

* Fixed adding and removing categories on content whose name contains certain special characters.

* Fixed styles and labels with special characters so they display as typed in the list, admin link and site components.

* Fixed the mode dispatcher component so that characters such as `<` or `&` in its mode display as text in edit mode.

* Fixed the table view of folders for files whose media type contains special characters, such as `&`.

* Fixed the category filter of content lists so that category titles containing `<`, `&` or quotes display as written.

* Fixed the "Link to Content Manager" component so it loads on published pages when its name, or the name of a page above it, contains certain special characters.

* Fixed the video component for video files whose media type contains certain special characters.

* Added maven.jahia.com to the Maven repositories legacy-default-components uses to download its dependencies.

* Fixed the current user component so that a style or view name containing `<`, `&` or quotes is kept intact in the page.

* Fixed the image slider view of lists so that images whose file names contain certain special characters display correctly.

* Fixed display of editor-set text and attribute values containing special characters across the legacy default components views (button/popup, author, metadata, video).

* Fixed the table view of content folders for items whose names contain certain special characters.

* Fixed the gallery view of lists so that file names containing special characters are shown as typed in the image title.

* Changed the file list's select view so that its delete control is shown to users who are allowed to delete the file.

* Fixed the details link of the site list component when the configured details view name contains HTML special characters such as `&`.

* Changed the web project creation component so that its form is shown to users who are allowed to create a web project.

* Fixed the display of titles and labels containing special characters in the legacy default components.

* Fixed list titles in the dropdown view so that characters such as `&`, `<` or quotes display as typed.

* Fixed the raw view of the metadata component so that creator and last contributor names containing special characters are displayed as text.
