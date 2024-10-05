package piat.opendatasearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import java.util.Collection;
import java.util.Map;

import com.google.gson.stream.JsonWriter;

/**
 * @author Iker Bermejo Iker.bluruena@alumnos.upm.es
 */

/**
 * Clase usada para crear un documento JSON.
 * Contiene un método estático llamado generar() que se encarga de crear un
 * fichero JSON a partir de la información que se le pasa como parámetro.
 */
public class GenerarJSON
{
	/**
	 * Constructor privado para que esta clase no se pueda instanciar
	 */
	private GenerarJSON ()
	{
	}

	/**
	 * Método que se encarga de generar un documento JSON a partir de la
	 * información que se le pasa como parámetro.
	 * @param salJSON Fichero en el que almacenar el documento JSON.
	 * @param query La categoría objeto de la búsqueda de información.
	 * @param remRes Número de recursos encontrados.
	 * @param locations Ubicaciones de los recursos encontrados.
	 * @param datasetRes Los identificadores de los datasets junto con el
	 * número de recursos que cumplen el criterio de selección.
	 */
	public static void generar (
			String salJSON, // Archivo en el que se almacenará el documento JSON
	        String query, // Categoría de la búsqueda de información
	        int numRes, // Número de recursos encontrados
	        Collection<String> locations, // Ubicaciones de los recursos encontrados
	        Map<String, Integer> datasetRes // Identificadores de los datasets y el número de recursos correspondientes
	        )
	throws Exception
	{

		/* TODO: instanciar un objeto JsonWriter para gestionar el proceso de creación del JSON */
		
		try (OutputStream os = new FileOutputStream(salJSON); // Crear un OutputStream para escribir en el archivo especificado por salJSON
	             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8); // Crear un OutputStreamWriter para escribir caracteres en el OutputStream usando UTF-8
	             JsonWriter writer = new JsonWriter(osw)) { // Crear un JsonWriter para escribir JSON en el OutputStreamWriter
         //JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(new File(salJSON)), StandardCharsets.UTF_8));
			
			writer.setIndent("  "); // Establecer la indentación para formatear el JSON

		/* TODO: inicialización del documento JSON */
			writer.beginObject();
		/* TODO: escribir Query */
			writer.name("query").value(query);
		/* TODO: escribir numRes */
			writer.name("numeroResources").value(numRes);
		/* TODO: escribir datasetRes */
	    // Escribir los datasets y el número de recursos asociados
			writer.name("infDatasets");
		    writer.beginArray(); // Iniciar el array de datasets
			 for (Map.Entry<String, Integer> entry : datasetRes.entrySet()) {
			     writer.beginObject(); // Iniciar el objeto dataset
			     writer.name("id").value(entry.getKey()); // Escribir el identificador del dataset
			     writer.name("num").value(entry.getValue()); // Escribir el número de recursos del dataset
			     writer.endObject(); // Finalizar el objeto dataset
			            }
			            writer.endArray(); // Finalizar el array de datasets
		/* TODO: escribir locations */
            writer.name("ubicaciones");
            writer.beginArray();
            for (String location : locations) {
                writer.value(location);// Escribir cada ubicación
            }
            writer.endArray();
		/* TODO: finalizar el documento */
            writer.endObject();
        } catch (IOException e) {
            throw new Exception("Error al generar el documento JSON", e);
        }

	}
}
