package cz.ladicek.dmrdiff;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.ManagementProtocol;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadResourceOption;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * mvn clean package exec:java -Dexec.mainClass=cz.ladicek.dmrdiff.Dump -Dexec.args=output.dmr
 */
public final class Dump {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: " + Dump.class.getName() + " <output file>");
            System.exit(1);
        }

        OnlineOptions options = OnlineOptions.standalone()
                .localDefault()
                .protocol(ManagementProtocol.HTTP_REMOTING)
                .build();

        try (OnlineManagementClient client = ManagementClient.online(options)) {
            ModelNodeResult result = new Operations(client).readResource(Address.root(),
                    ReadResourceOption.INCLUDE_DEFAULTS, ReadResourceOption.RECURSIVE);

            write(result, args[0]);
        }
    }

    private static void write(ModelNode modelNode, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            // I know there's a race here, but this is just for convenience
            System.err.println("File already exists: " + fileName);
            System.exit(1);
            return; // dead code
        }

        if (fileName.endsWith(".dmr")) {
            Files.write(path, modelNode.toString().getBytes(StandardCharsets.US_ASCII));
        } else if (fileName.endsWith(".json")) {
            Files.write(path, modelNode.toJSONString(false).getBytes(StandardCharsets.UTF_8));
        } else {
            System.err.println("Output file must have extension .dmr or .json, was: " + fileName);
            System.exit(1);
        }
    }
}
