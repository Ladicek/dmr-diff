package cz.ladicek.dmrdiff;

import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * mvn clean package exec:java -Dexec.mainClass=cz.ladicek.dmrdiff.Main -Dexec.args="input-old.dmr input-new.dmr output.txt"
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2 && args.length != 3) {
            System.out.println("Usage: " + Main.class.getName() + " <old input file> <new input file> [<output file>]");
            System.exit(1);
        }

        ModelNode oldInput = read(args[0]);
        ModelNode newInput = read(args[1]);

        Diff diff = DiffAlgorithm.compute(oldInput, newInput);

        if (args.length == 3) {
            Files.write(Paths.get(args[2]), diff.describe().getBytes(StandardCharsets.UTF_8));
        } else {
            System.out.println(diff.describe());
        }
    }

    private static ModelNode read(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            // I know there's a race here, but this is just for convenience
            System.err.println("File not found: " + fileName);
            System.exit(1);
            return null; // dead code
        }

        if (fileName.endsWith(".json")) {
            return ModelNode.fromJSONStream(Files.newInputStream(path));
        } else if (fileName.endsWith(".dmr")) {
            return ModelNode.fromStream(Files.newInputStream(path));
        } else {
            System.err.println("Input file must have extension .dmr or .json, was: " + fileName);
            System.exit(1);
            return null; // dead code
        }
    }
}
