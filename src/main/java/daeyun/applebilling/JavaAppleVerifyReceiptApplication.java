package daeyun.applebilling;

import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@SpringBootApplication
public class JavaAppleVerifyReceiptApplication {
        
    private static String itunesUrl = "https://buy.itunes.apple.com";
    private static String sandboxUrl = "https://sandbox.itunes.apple.com" ;
    /**
     *애플에서 받은 키 정보 
     */
    private static String appleSecretKey = "";
    
	public static void main(String[] args) throws UnirestException {
	    /**
	     * 영수증 정보 
	     */
	    String receiptData = "";
	    
	    JSONObject bodyData = new JSONObject()
                .put("receipt-data", receiptData)
                .put("password", appleSecretKey)
                .put("exclude-old-transactions", true);
		HttpResponse<JsonNode> response = Unirest.post(itunesUrl+"/verifyReceipt")
                .header("Content-Type", "application/json")
                .body(bodyData)
                .asJson();
        /**
         * sandbox 영수증인 경우 
         */
        if(response.getStatus() == 200 && response.getBody().getObject().get("status").toString().equals("21007")) {
            response = Unirest.post(sandboxUrl+"/verifyReceipt")
                    .header("Content-Type", "application/json")
                    .body(bodyData)
                    .asJson();
        }
        if(response.getStatus() == 200 && response.getBody().getObject().get("status").toString().equals("0")) {
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject)parser.parse(response.getBody().getObject().get("receipt").toString());
            JsonArray array = (JsonArray)parser.parse(object.get("in_app").toString());
            // 최근 결제 내역 가져오기 
            String transaction_id = null;
            String original_transaction_id = null;           
            String purchase_date_ms = null;            
            String product_id = null;
            for(int i = 0 ; i< array.size() ; i ++) {
                object = (JsonObject)parser.parse(array.get(i).toString());
                if(transaction_id == null  || Long.parseLong(purchase_date_ms) < Long.parseLong(object.get("purchase_date_ms").toString().replaceAll("\"",""))) {
                    transaction_id  = object.get("transaction_id").toString().replaceAll("\"","");
                    original_transaction_id = object.get("original_transaction_id").toString().replaceAll("\"","");
                    purchase_date_ms = object.get("purchase_date_ms").toString().replaceAll("\"","");  
                    product_id = object.get("product_id").toString().replaceAll("\"","");
                }
            }
            System.out.println(transaction_id);
            System.out.println(original_transaction_id);
            System.out.println(purchase_date_ms);
            System.out.println(product_id);
        }
	}

}
