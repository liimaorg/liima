window.onload = function() {
  window.ui = SwaggerUIBundle({
    url: "resources/openapi.json",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
    tagsSorter: "alpha"
  });
};
