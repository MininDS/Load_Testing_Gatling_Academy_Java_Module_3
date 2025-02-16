//Created class Categories in gatlingdempstoreapi package
package gatlingdemostoreapi;


//Imported some needed Java-libs for code-execution
import java.util.Map;


//Created class Headers with constant headers in the system api - e.g. authorization token
public class Headers {
    public static Map<CharSequence, String> authorizationHeaders = Map.ofEntries(
            Map.entry("authorization", "Bearer #{jwt}")
    );
}
