BOWER
======
The Javascript libraries shall be managed with the help of bower.
Therefore, a bower configuration is included in this package to keep
track of the different versions as well as to have an opportunity to
document the reasons for the inclusion of a specific package.

Since we don't want everybody to be forced to have a bower installation,
the update of the JavaScript is made manually by simply invoking
"bower install"
in the console of the AMW_web project root directory.

The downloaded libraries will then be checked into the GIT repository.

IMPORTANT: DO NOT MAKE ANY CHANGES TO SOURCE FILES MANAGED BY BOWER
They can be replaced/overridden at any time. If you have to customize styles,
or other things please make sure, you place them appropriately.