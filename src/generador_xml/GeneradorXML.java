package generador_xml;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
//import java.security.KeyFactory;
//import java.security.PrivateKey;
//import java.security.spec.PKCS8EncodedKeySpec;
import org.update4j.Configuration;
import org.update4j.FileMetadata;

/**
 * @author
 * Daros Ledezma
 */

public class GeneradorXML {
    public static void main(String[] args) throws Exception {
        
        // --- AQUÍ SE IMPLEMENTA LA CARGA DE LA LLAVE ---
        /*
        byte[] keyBytes = Files.readAllBytes(Paths.get("private.der"));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey key = kf.generatePrivate(spec);
        */
        // -----------------------------------------------

        // Asegúrate de que 'key' esté cargada antes de esto
        Configuration config = Configuration.builder()

            // nombre con que se registra la app en el XML
            .property("app.name","Jodasaly")

            // Automatically resolves system property
            .property("ruta.usuario", "${user.home}/AppData/Local/Jodasaly")

            // direccion del repositorio
            .baseUri("https://raw.githubusercontent.com/BurstChamploo/repositorio_jodasaly/master") 
            
            // dirección de destino de la descarga
            .basePath("${ruta.usuario}")
            
            // para añadir al classpath al descargar (se usa con .jar de la app)
            .file(FileMetadata.readFrom("app/jodasaly_app.jar") // ruta en mi workspace
                .uri("app/jodasaly_app.jar") //baseUri + uri (de donde se descarga en el repositorio)
                .path("app/jodasaly_app.jar") // basePath + path (donde se almacena en la maquina cliente)
                .classpath() // classpath -> true
            )

            // Solo descarga (se usa para las dependencias .jar)
            /*
            .file(FileMetadata.readFrom("")
                .path("null")
                .uri("null")
            )
            */
            .launcher("launcher.Lanzador")

            // aqui se inyecta la llave de seguridad
            //.signer(key)
            // indica cuales son los archivos del proyecto que se deben
            // registrar en el XML

            // registra las dependencias al classpath
            
            .files(FileMetadata.streamDirectory("img/") // se descargan de baseUri + img/ y se guardan en basePath + img/
                    .filter(r -> r.getSource().toString().endsWith(".png"))
                    .map(r -> r.uri(r.getSource().toString())  // baseUri + ruta
                        .path(r.getSource()) // basePath + ruta
                        ))
            
            .build();

        try (Writer out = Files.newBufferedWriter(Paths.get("config.xml"))) {
            config.write(out);
            System.out.println("¡Configuración generada con éxito en la carpeta de actualizaciones!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
