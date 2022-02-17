package ca.bc.gov.nrs.wfdm.wfdm_clamav_scan_handler;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Static handler for WFDM API Access.
 */
public class GetFileFromWFDMAPI {
  
  
  // Private constructor hides the implicit public constructor
  private GetFileFromWFDMAPI() { /* empty */ }

  /**
   * Fetch an Access Token for authentication with the WFDM API
   * 
   * @param client   The Client ID
   * @param password The Client Secret
   * @return
   * @throws UnirestException
   */
  public static String getAccessToken (String client, String password) throws UnirestException {
    HttpResponse<JsonNode> httpResponse = Unirest.get(System.getenv("WFDM_DOCUMENT_TOKEN_URL").trim())
        .basicAuth(client, password)
        .asJson();

    if (httpResponse.getStatus() == 200) {
      JSONObject responseBody = httpResponse.getBody().getObject();
      return responseBody.get("access_token").toString();
    } else {
      return null;
    }
  }

  /**
   * Fetch the details of a WFDM File resource, including Metadata and security
   * This method will not return the files bytes
   * 
   * @param accessToken
   * @param fileId
   * @return
   * @throws UnirestException
   */
  public static String getFileInformation (String accessToken, String fileId) throws UnirestException {
    HttpResponse<String> detailsResponse = Unirest.get(System.getenv("WFDM_DOCUMENT_API_URL").trim() + fileId)
        .header("Authorization", "Bearer " + accessToken)
        .header("Content-Type", "application/json").asString();

    if (detailsResponse.getStatus() == 200) {
      return detailsResponse.getBody();
    } else {
      return null;
    }
  }

  public static boolean setVirusScanMetadata (String accessToken, String fileId, String versionNumber, JSONObject fileDetails, String status) throws UnirestException {
    // Add metadata to the File details to flag it as "Unscanned"
    JSONArray metaArray = fileDetails.getJSONArray("metadata");
    // Locate any existing scan meta and remove
    for (int i = 0; i < metaArray.length(); i++) {
      String metadataName = metaArray.getJSONObject(i).getString("metadataName");
      if (metadataName.equalsIgnoreCase("wfdm-system-scanStatus-" + versionNumber)) {
        metaArray.remove(i);
        break;
      }
    }

    // inject scan meta
    JSONObject meta = new JSONObject();
    meta.put("@type", "http://resources.wfdm.nrs.gov.bc.ca/fileMetadataResource");
    meta.put("metadataName", "wfdm-system-scanStatus-" + versionNumber);
    meta.put("metadataValue", status);
    metaArray.put(meta);

    // PUT the changes
    HttpResponse<String> metaUpdateResponse = Unirest.put(System.getenv("WFDM_DOCUMENT_API_URL").trim() + fileId)
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + accessToken)
        .body(fileDetails.toString())
        .asString();

    return metaUpdateResponse.getStatus() == 200;
  }
}
