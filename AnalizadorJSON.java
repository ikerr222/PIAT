package piat.opendatasearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * @author Iker Bermejo Lurueña Iker.bluruena@alumnos.upm.es
 */


/**
 * Clase que implementa el analizador JSON usando un JsonReader.
 * Implementa la interfaz ParserRecursos para que pueda ser usada por la
 * aplicación para obtener la información de los recursos pertinentes.
 */

class AnalizadorJSON implements ParserRecursos
{
    private final Collection<Map<String,String>> resources; // Almacena los recursos pertinentes extraídos del JSON
    private final JsonReader reader; // Lee y analiza el documento JSON
    private final Collection<String> concepts; // Lista de conceptos pertinentes a buscar en el JSON
    private final String entrada; // URL del documento JSON a analizar
    private boolean valido = false; // Indica si un recurso es válido y debe ser almacenado

    /**
     * Constructor de la clase.
     * @param entrada url al documento JSON a analizar
     * @param concepts los concepts pertinentes
     * @throws IOException 
     * @throws MalformedURLException 
     */
    public AnalizadorJSON(String entrada, Collection<String> concepts) throws MalformedURLException, IOException
    {
        this.entrada = entrada;
        this.concepts = concepts;
        this.resources = new ArrayList<Map<String,String>>();
        
        URL url = new URL(entrada); // Crea un objeto URL a partir de la cadena de entrada (URL del documento JSON)
        InputStream is = url.openStream(); // Abre un flujo de entrada desde la URL
        final Reader read = new InputStreamReader(is); // Crea un InputStreamReader para leer el flujo de entrada con el formato adecuado
        final BufferedReader jsonURL = new BufferedReader(read); // Envuelve el InputStreamReader en un BufferedReader para una lectura eficiente
        this.reader = new JsonReader(jsonURL); // Crea un JsonReader a partir del BufferedReader para analizar el documento JSON
    }

    /**
     * Analiza el documento JSON que contiene recursos dentro del array "@graph".
     * Únicamente debe obtener los 5 primeros recursos pertinentes.
     */
    @Override
    public void analizarRecursos() throws Exception
    {
    	reader.beginObject(); // Inicia la lectura del objeto JSON ({})
        while (reader.hasNext()) { // Itera mientras haya más elementos en el objeto JSON
            String name = reader.nextName(); // Obtiene el nombre del siguiente elemento
            switch (name) { // Evalúa el nombre del elemento
                case "@graph": // Si el nombre es "@graph"
                    reader.beginArray(); // Inicia la lectura del array JSON
                    procesarGraph(); // Llama al método para procesar los elementos del array
                    reader.endArray(); // Termina la lectura del array JSON
                    break;
                default: // Si el nombre no es "@graph"
                    reader.skipValue(); // Omite el valor del elemento actual
                    break;
            }
        }
        reader.endObject(); // Termina la lectura del objeto JSON
    }

    /**
     * Procesa el array "@graph" del documento JSON.
     * @throws IOException
     */
    public void procesarGraph() throws IOException {
        int resourceCount = 0;  // Inicializa el contador de recursos
        while (reader.hasNext() && resourceCount < 5) { // Itera mientras haya más elementos en el array y se hayan procesado menos de 5 recursos
            Map<String,String> resource = new HashMap<String,String>(); // Crea un nuevo mapa para el recurso actual
            reader.beginObject(); // Inicia la lectura del objeto JSON (Resource)
            procesarRecurso(resource); // Llama al método para procesar las propiedades del recurso
            reader.endObject(); // Termina la lectura del objeto JSON

            if (valido) { // Si el recurso es válido
                resources.add(resource); // Añade el recurso a la colección de recursos
                resourceCount++;  // Incrementa el contador de recursos
                valido = false; // Resetea la validez del recurso para el próximo ciclo
            }
        }
        while (reader.hasNext()) { // Si hay elementos restantes en el array
            reader.skipValue(); // Omite los valores restantes
        }
    }

    @Override
    public Collection<Map<String,String>> getRecursos()
    {
        return resources;// Devuelve la colección de recursos procesados.
    }

    private void procesarRecurso(Map<String,String> resource) throws IOException {
        String type = null, title = null, start = null, end = null, link = null, eventLocation = null;
        String idA = null, latitude = null, longitude = null, locality = null, street = null;

        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "@type":
                    type = reader.nextString();
                    // Verifica si el tipo del recurso coincide con algún concepto relevante. 
                    for (String concept : concepts) {
                        if (type.equals(concept)) {
                            valido = true;
                            resource.put("id", entrada);
                            resource.put("type", type);
                            break;
                        }
                    }
                    break;
                case "link":
                    if (valido) {
                        link = reader.nextString();
                        resource.put("link", link);
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "title":
                    if (valido) {
                        title = reader.nextString();
                        resource.put("title", title);
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "description":
                    if (valido) {
                        resource.put("description", reader.nextString());
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "dtstart":
                    if (valido) {
                        start = reader.nextString();
                        resource.put("start", start);
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "dtend":
                    if (valido) {
                        end = reader.nextString();
                        resource.put("end", end);
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "event-location":
                    if (valido) {
                        eventLocation = reader.nextString();
                        resource.put("eventlocation", eventLocation);
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "address":
                    if (valido) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            switch (reader.nextName()) {
                                case "area":
                                    reader.beginObject();
                                    while (reader.hasNext()) {
                                        switch (reader.nextName()) {
                                            case "@id":
                                                idA = reader.nextString();
                                                resource.put("idA", idA);
                                                break;
                                            case "locality":
                                                if (reader.peek() == JsonToken.STRING) {
                                                    locality = reader.nextString();
                                                    resource.put("locality", locality);
                                                } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                                                    reader.beginObject();
                                                    while (reader.hasNext()) {
                                                        String localityName = reader.nextName();
                                                        if (localityName.equals("locality")) {
                                                            locality = reader.nextString();
                                                            resource.put("locality", locality);
                                                        } else {
                                                            reader.skipValue();
                                                        }
                                                    }
                                                    reader.endObject();
                                                }
                                                break;
                                            case "street-address":
                                                if (reader.peek() == JsonToken.STRING) {
                                                    street = reader.nextString();
                                                    resource.put("street", street);
                                                } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                                                    reader.beginObject();
                                                    while (reader.hasNext()) {
                                                        String streetName = reader.nextName();
                                                        if (streetName.equals("street-address")) {
                                                            street = reader.nextString();
                                                            resource.put("street", street);
                                                        } else {
                                                            reader.skipValue();
                                                        }
                                                    }
                                                    reader.endObject();
                                                }
                                                break;
                                            default:
                                                reader.skipValue();
                                                break;
                                        }
                                    }
                                    reader.endObject();
                                    break;
                                case "@id":
                                    idA = reader.nextString();
                                    resource.put("idA", idA);
                                    break;
                                case "locality":
                                    locality = reader.nextString();
                                    resource.put("locality", locality);
                                    break;
                                case "street-address":
                                    street = reader.nextString();
                                    resource.put("street", street);
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "location":
                    if (valido) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            switch (reader.nextName()) {
                                case "latitude":
                                    latitude = reader.nextString();
                                    resource.put("latitude", latitude);
                                    break;
                                case "longitude":
                                    longitude = reader.nextString();
                                    resource.put("longitude", longitude);
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
    }
}