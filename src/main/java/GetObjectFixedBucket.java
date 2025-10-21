import java.io.IOException;
import java.io.OutputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
 
@WebServlet(urlPatterns = { "/object/*" })
public class GetObjectFixedBucket extends HttpServlet {
    
private static final long serialVersionUID = 1L;
 
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
                throws ServletException, IOException {
resp.setContentType("image/jpeg/png");
 
String pathInfo = req.getPathInfo(); 
String[] pathParts = pathInfo.split("/");
String key = pathParts[1]; 
String bucketName = "p-acl";
 
AwsBasicCredentials awsBasicCredentials 
                        = AwsBasicCredentials
                                .create("accessKeyId",
                                 "secretAccessKey");

AwsCredentialsProvider awsCredentialsProvider;
awsCredentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
 
S3Client s3Client = 
                        S3Client.builder()
                                .credentialsProvider(awsCredentialsProvider).region(Region.US_EAST_1)
.build();
 
GetObjectRequest request = 
                        GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build();
 
                OutputStream outputStream;
                ResponseInputStream<GetObjectResponse> response 
                        = s3Client.getObject(request);
                outputStream = resp.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = response.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
outputStream.close();
}
}