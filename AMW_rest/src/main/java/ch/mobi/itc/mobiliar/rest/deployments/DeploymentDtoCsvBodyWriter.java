package ch.mobi.itc.mobiliar.rest.deployments;

import ch.mobi.itc.mobiliar.rest.dtos.AppWithVersionDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentDTO;
import ch.mobi.itc.mobiliar.rest.dtos.DeploymentParameterDTO;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A class with similar functionality exists in the AMW_web package { @link: ch.puzzle.itc.mobiliar.presentation.deploy.DeploymentCSVExport.class }
 */
@Provider
@Produces({"text/comma-separated-values", "text/csv"})
public class DeploymentDtoCsvBodyWriter implements MessageBodyWriter<List<DeploymentDTO>>{

    private static final String CSV_SEPARATOR = ConfigurationService.getProperty(ConfigKey.CSV_SEPARATOR);

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (List.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArgs = (parameterizedType.getActualTypeArguments());
            return (actualTypeArgs.length == 1 && actualTypeArgs[0].equals(DeploymentDTO.class));
        }
        return false;
    }

    @Override
    public long getSize(List<DeploymentDTO> deploymentDTOS, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        //used for contentLength header, -1 = calculate it
        return -1;
    }

    @Override
    public void writeTo(List<DeploymentDTO> deploymentDTOS, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream outputStream) throws WebApplicationException {
        PrintWriter pw = new PrintWriter(outputStream);

        httpHeaders.add(HttpHeaders.CONTENT_ENCODING, "utf-8");
        httpHeaders.add(HttpHeaders.CACHE_CONTROL, "private, must-revalidate");
        httpHeaders.add("Pragma", "cache");
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        httpHeaders.add("Content-Disposition", "attachment; filename=deployments_"
                + new SimpleDateFormat("yyyy-MM-dd_HHmm").format(new Date()) + ".csv");

        pw.append("Id").append(CSV_SEPARATOR)
                .append("Tracking Id").append(CSV_SEPARATOR)
                .append("Deployment state").append(CSV_SEPARATOR)
                .append("Build success").append(CSV_SEPARATOR)
                .append("Deployment executed").append(CSV_SEPARATOR)
                .append("App server").append(CSV_SEPARATOR)
                .append("Applications").append(CSV_SEPARATOR)
                .append("Deployment release").append(CSV_SEPARATOR)
                .append("Environment").append(CSV_SEPARATOR)
                .append("Environment Alias").append(CSV_SEPARATOR)
                .append("Target platform").append(CSV_SEPARATOR)
                .append("Deployment parameters").append(CSV_SEPARATOR)
                .append("Creation date").append(CSV_SEPARATOR)
                .append("Request user").append(CSV_SEPARATOR)
                .append("Deployment date").append(CSV_SEPARATOR)
                .append("Configuration to deploy").append(CSV_SEPARATOR)
                .append("Deployment confirmed").append(CSV_SEPARATOR)
                .append("Confirmation date").append(CSV_SEPARATOR)
                .append("Confirmation user").append(CSV_SEPARATOR)
                .append("Cancel date").append(CSV_SEPARATOR)
                .append("Cancel user").append(CSV_SEPARATOR)
                .append("Status message").append(CSV_SEPARATOR)
                .append('\n');

        for (DeploymentDTO dto : deploymentDTOS) {
            pw.append(dto.getId().toString()).append(CSV_SEPARATOR)
                    .append(dto.getTrackingId().toString()).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getState().getDisplayName())).append(CSV_SEPARATOR)
                    .append(doubleQuote(Boolean.toString(dto.isBuildSuccess()))).append(CSV_SEPARATOR)
                    .append(doubleQuote(Boolean.toString(dto.isDeploymentExecuted()))).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getAppServerName())).append(CSV_SEPARATOR)
                    .append(formatAppsWithVersion(dto.getAppsWithVersion())).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getReleaseName())).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getEnvironmentName())).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getEnvironmentNameAlias())).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getTargetPlatform())).append(CSV_SEPARATOR)
                    .append(formatDeploymentParameters(dto.getDeploymentParameters())).append(CSV_SEPARATOR)
                    .append(formatDate(dto.getDeploymentJobCreationDate())).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getRequestUser())).append(CSV_SEPARATOR)
                    .append(formatDate(dto.getDeploymentDate())).append(CSV_SEPARATOR)
                    .append(formatDate(dto.getStateToDeploy())).append(CSV_SEPARATOR)
                    .append(doubleQuote(Boolean.toString(dto.isDeploymentConfirmed()))).append(CSV_SEPARATOR)
                    .append(formatDate(dto.getDeploymentConfirmationDate())).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getConfirmUser())).append(CSV_SEPARATOR)
                    .append(formatDate(dto.getDeploymentCancelDate())).append(CSV_SEPARATOR)
                    .append(doubleQuote(dto.getCancelUser())).append(CSV_SEPARATOR)
                    .append(formatStatusMessage(dto.getStatusMessage())).append(CSV_SEPARATOR)
                    .append('\n');
        }
        pw.flush();
    }

    protected String formatAppsWithVersion(List<AppWithVersionDTO> dtos) {
        StringBuilder sb = new StringBuilder();
        for (AppWithVersionDTO dto : dtos) {
            sb.append(dto.getApplicationName()).append(" ").append(dto.getVersion()).append('\n');
        }
        return stripTrailingLineFeedAndSurroundWithDoubleQuotes(sb);
    }

    protected String formatDeploymentParameters(List<DeploymentParameterDTO> dtos) {
        StringBuilder sb = new StringBuilder();
        for (DeploymentParameterDTO dto : dtos) {
            sb.append(dto.getKey()).append(" ").append(dto.getValue()).append('\n');
        }
        return stripTrailingLineFeedAndSurroundWithDoubleQuotes(sb);
    }

    private String stripTrailingLineFeedAndSurroundWithDoubleQuotes(StringBuilder sb) {
        sb.insert(0, '"');
        int length = sb.length();
        if (length > 1) {
            sb.deleteCharAt(length-1);
        }
        return sb.append('"').toString();
    }

    protected String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("\"yyyy-MM-dd HH:mm\"").format(date);
    }

    protected String formatStatusMessage(String message) {
        if (message == null) {
            return "";
        }
        String formatted = message.replaceAll("\"", "");
        return stripTrailingLineFeedAndSurroundWithDoubleQuotes(formatted);
    }

    private String stripTrailingLineFeedAndSurroundWithDoubleQuotes(String string) {
        if (string.lastIndexOf("\n") == string.length()-1) {
            string = string.substring(0, string.length()-1);
        }
        return doubleQuote(string);
    }

    private String doubleQuote(String string) {
        if (string == null) {
            return "";
        }
        return "\"" + string + "\"";
    }
}
