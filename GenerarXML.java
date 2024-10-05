package piat.opendatasearch;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Iker Bermejo Lurueña Iker.bluruena@alumnos.upm.es
 */

/**
 * Clase estática para crear un String que contenga el documento xml a partir
 * de la información de concepts y datasets pertinentes. 
 */	
public class GenerarXML
{
	
	private static final String cabecera = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE searchResults [\n"
			+ "	<!ENTITY ldquo \"entity-value\">\n"
			+ "	<!ENTITY rdquo \"entity-value\">\n"
			+ "]>\n" +
            "<searchResults xmlns=\"http://piat.dte.upm.es/practica4\"\n" +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "xsi:schemaLocation=\"http://www.piat.dte.upm.es/practica4 ResultadosBusquedaP4.xsd\">";
	
	/**
	 * Constructor privado para que esta clase no se pueda instanciar
	 */
	private GenerarXML ()
	{
	}

	/**
	 * Método que deberá ser invocado desde el programa principal.
	 * @param categoría 
	 * @param concepts la información de los concepts pertinentes.
	 * @param datasets la información de los datasets pertinentes.
	 * @return String con el documento XML de salida.
	 */
	public static String generar(String categoría, Collection<String> concepts, 
			Map<String, Map<String, String>> datasets, Collection<Map<String, String>> recursos) {
		
		final StringBuilder salidaXML = new StringBuilder();
		
		salidaXML.append(cabecera); 
	
		// Añadir la información de <summary>
		
		salidaXML.append("<summary>\n");
		salidaXML.append("<query>").append(categoría).append("</query>\n");
		salidaXML.append("<numConcepts>").append(concepts.size()).append("</numConcepts>\n");
		salidaXML.append("<numDatasets>").append(datasets.size()).append("</numDatasets>\n");
		salidaXML.append("</summary>\n");

        //Resultados
		
		salidaXML.append("<results>\n");
		
		//Concepts
		
		salidaXML.append("<concepts>\n");
		for (String concept : concepts) {
	        salidaXML.append("<concept>").append(concept).append("</concept>\n");
	    }
        salidaXML.append("</concepts>\n");

        //Datasets
        
        salidaXML.append("<datasets>\n");
        
        for(String IdDataset : datasets.keySet()) {
			
			salidaXML.append(("\n\t\t\t<dataset id=\"#IDPATTERN#\">").replace("#IDPATTERN#", IdDataset));
			LinkedHashMap<String,String> aux = (LinkedHashMap<String, String>) datasets.get(IdDataset);
			// Tiene que ser LinkedHashMap para que almacene las cosas en el mapa en el ORDEN en el que se METEN
			
			for(String key : aux.keySet()) {
				if(key.equals("title"))
					salidaXML.append(("\n\t\t\t\t<title>"+"#TITLE#"+"</title>").replace("#TITLE#", aux.get(key)));
				if(key.equals("description"))
					salidaXML.append(("\n\t\t\t\t<description>"+"#DESCRIPTION#"+"</description>").replace("#DESCRIPTION#", aux.get(key)));
				if(key.equals("theme"))
					salidaXML.append(("\n\t\t\t\t<theme>"+"#THEME#"+"</theme>").replace("#THEME#", aux.get(key)));
			}
			salidaXML.append("\n\t\t\t</dataset>");
		}
		
        salidaXML.append("</datasets>\n");

        
     // Resources
        salidaXML.append("\n\t\t<resources>");

        for (Map<String, String> recurso : recursos) {
            salidaXML.append("\n\t<resource id=\"").append(recurso.get("id")).append("\">");
            salidaXML.append("\n\t<concept id=\"").append(recurso.get("type")).append("\"/>");

            // Incluir link, incluso si es una etiqueta vacía
            if (recurso.get("link") != null) {
                salidaXML.append("\n\t<link><![CDATA[").append(recurso.get("link")).append("]]></link>");
            }
            // Incluir title, incluso si es una etiqueta vacía
            if (recurso.get("title") != null) {
                salidaXML.append("\n\t<title>").append(recurso.get("title")).append("</title>");
            } else {
                salidaXML.append("\n\t<title></title>");
            }

            // Incluir location
            salidaXML.append("\n\t<location>");

            // Incluir eventLocation, solo si existe
            if (recurso.get("eventlocation") != null) {
                salidaXML.append("\n\t<eventLocation>").append(recurso.get("eventlocation")).append("</eventLocation>");
            } 

            // Incluir address
            salidaXML.append("\n\t<address>");
            
            if (recurso.get("idA") != null) {
                salidaXML.append("\n\t<area>").append(recurso.get("idA")).append("</area>");
            } 

            if (recurso.get("locality") != null) {
                salidaXML.append("\n\t<locality>").append(recurso.get("locality")).append("</locality>");
            } 

            if (recurso.get("street") != null) {
                salidaXML.append("\n\t<street>").append(recurso.get("street")).append("</street>");
            } 

            salidaXML.append("\n\t</address>");

            // Incluir timetable
            salidaXML.append("\n\t<timetable>");
            
            if (recurso.get("start") != null) {
                salidaXML.append("\n\t<start>").append(recurso.get("start")).append("</start>");
            } else {
                salidaXML.append("\n\t<start></start>");
            }

            if (recurso.get("end") != null) {
                salidaXML.append("\n\t<end>").append(recurso.get("end")).append("</end>");
            } else {
                salidaXML.append("\n\t<end></end>");
            }

            salidaXML.append("\n\t</timetable>");

            // Incluir georeference solo si ambos valores están presentes
            if (recurso.get("latitude") != null && recurso.get("longitude") != null) {
                salidaXML.append("\n\t<georeference>").append(recurso.get("latitude")).append("\t").append(recurso.get("longitude")).append("</georeference>");
            }

            salidaXML.append("\n\t</location>");

            // Incluir description, incluso si es una etiqueta vacía
            if (recurso.get("description") != null && !recurso.get("description").trim().isEmpty()) {
                salidaXML.append("\n\t<description><![CDATA[").append(recurso.get("description")).append("]]></description>");
            } 

            salidaXML.append("\n\t</resource>");
            }

        salidaXML.append("\n\t\t</resources>");
        
        
        //FIN
        salidaXML.append("</results>\n");
        salidaXML.append("</searchResults>\n");
		
		return salidaXML.toString();
	}
}
