/**
 * Configuration for head elements added during the creation of index.html.
 *
 * All href attributes are added the publicPath (if exists) by default.
 * You can explicitly hint to prefix a publicPath by setting a boolean value to a key that has
 * the same name as the attribute you want to operate on, but prefix with = (this is not working at the moment)
 *
 * Example: ()
 * { name: 'msapplication-TileImage', content: '/assets/icon/ms-icon-144x144.png', '=content': true },
 * Will prefix the publicPath to content.
 *
 * { rel: 'apple-touch-icon', sizes: '57x57', href: '/assets/icon/apple-icon-57x57.png', '=href': false },
 * Will not prefix the publicPath on href (href attributes are added by default
 *
 */
module.exports = {
  link: [
    /** <link> tags for 'apple-touch-icon' (AKA Web Clips). **/
    { rel: 'apple-touch-icon', sizes: '76x76', href: '/AMW_angular/assets/icon/apple-touch-icon.png' },

    /** <link> tags for android web app icons **/
    { rel: 'icon', type: 'image/png', sizes: '72x72', href: '/AMW_angular/assets/icon/android-chrome-72x72.png' },

    /** <link> tags for favicons **/
    { rel: 'shortcut icon', href: '/AMW_angular/assets/icon/favicon.ico' },
    { rel: 'icon', type: 'image/png', sizes: '32x32', href: '/AMW_angular/assets/icon/favicon-32x32.png' },
    { rel: 'icon', type: 'image/png', sizes: '16x16', href: '/AMW_angular/assets/icon/favicon-16x16.png' },

    /** <link> tags for a Web App Manifest **/
    { rel: 'manifest', href: '/AMW_angular/assets/manifest.json' }
  ],
  meta: [
    { name: 'msapplication-TileColor', content: '#000000' },
    { name: 'msapplication-TileImage', content: '/AMW_angular/assets/icon/mstile-150x150.png' },
    { name: 'theme-color', content: '#ffffff' }
  ]
};
