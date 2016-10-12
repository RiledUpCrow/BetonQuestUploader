/**
 * 
 */
package pl.betoncraft.betonquestuploader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Command line tool for sending packages via HTTP POST requests.
 *
 * @author Jakub Sapalski
 */
public class Sender {

	public static void main(String[] args) {
		try {
			if (args.length != 4) {
				System.out.println("java -jar post.jar http://server:port name password file");
				return;
			}
			String server = args[0];
			String user = args[1];
			String pass = args[2];
			File file = new File(args[3]);
			if (!file.exists()) {
				System.out.println("File does not exist");
				return;
			}
			String boundary = new BigInteger(64, new Random()).toString(32);
			List<ByteBuffer> buffers = new LinkedList<>();
			ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
			buffers.add(buffer);
			buffer.put(("--" + boundary + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put(("Content-Disposition: form-data; name=\"user\"" + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put((user + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put(("--" + boundary + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put(("Content-Disposition: form-data; name=\"pass\"" + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put((pass + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put(("--" + boundary + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put(("Content-Disposition: form-data; name=\"file\"" + System.lineSeparator()).getBytes("UTF-8"));
			buffer.put(("Content-Type: application/octet-stream" + System.lineSeparator()).getBytes("UTF-8"));
			byte[] bytes = new byte[1024];
			int size = 0;
			long totalSize = 0;
			InputStream in = new FileInputStream(file);
			while ((size = in.read(bytes)) > 0) {
				totalSize += size;
				try {
					buffer.put(bytes, 0, size);
				} catch (BufferOverflowException e) {
					buffer = ByteBuffer.allocate(1024*1024);
					buffers.add(buffer);
					buffer.put(bytes, 0, size);
				}
			}
			in.close();
			buffer.put(("--" + boundary + "--" + System.lineSeparator()).getBytes("UTF-8"));
			URL url = new URL(server);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Length", Long.toString(totalSize));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				for (ByteBuffer b : buffers) {
					wr.write(b.array(), 0, b.position());
				}
			}
			try (DataInputStream re = new DataInputStream(conn.getInputStream())) {
				int b;
				while ((b = re.read()) != -1) {
					System.out.print((char) b);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
