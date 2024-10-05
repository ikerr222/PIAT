package piat.opendatasearch;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Iker Bermejo Iker.bluruena@alumnos.upm.es
 */

/**
 * Clase que genera el analizador XPATH mediante la implementación
 * de la interfaz ParserXPATH para que pueda ser usada por la
 * aplicación para obtener la información del documento pertinente.
 */

public class AnalizadorXPATH implements ParserXPATH
{
	private String query; // Almacena la categoría usada en la consulta
	private int numRes; // Almacena el número de recursos en el documento XML
	private Collection<String> locations; // Almacena las ubicaciones de eventos, eliminando duplicados
	private Map<String,Integer> datasetRes; // Almacena la relación de identificadores de datasets y el número de recursos correspondientes
	///* TODO: tipo */ xPath; // Objeto para realizar las consultas
	///* TODO: tipo */ doc; // Documento DOM

	private XPath xPath;
	private Document doc; // Documento DOM que representa el documento XML a analizar
	
	
	/**
	 * TODO: es recomendable definir variables de tipo String
	 * con cada una de las expresiones XPATH a evaluar
	 **/
	// Expresiones XPath
	
    private static final String XPATH_QUERY = "/searchResults/summary/query/text()";
    private static final String XPATH_NUM_RESOURCES = "count(/searchResults/results/resources/resource)";
    private static final String XPATH_LOCATIONS = "/searchResults/results/resources/resource/location/eventLocation/text()";
    private static final String XPATH_DATASETS = "/searchResults/results/datasets/dataset";
    //"count(//asignatura[@departamento='D01']/creditosTeoricos)" cuenta el número de elementos <asignatura> que tienen un atributo departamento con el valor D01 y que contienen un elemento hijo <creditosTeoricos>

	/**
	 * Constructor de la clase.
	 * @param docXML url al documento XML a analizar
	 */
	public AnalizadorXPATH ( InputSource docXML ) throws Exception
	{
		// TODO: codificar el constructor
		
		//DOM
		// Crear una fábrica de constructores de documentos
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    // Crear un constructor de documentos
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Parsear el documento XML y obtener el objeto DOM
        this.doc = builder.parse(docXML);
        // Crear un objeto XPath para realizar consultas XPath
        this.xPath = XPathFactory.newInstance().newXPath();
        // Inicializar la colección de ubicaciones de eventos como un HashSet para eliminar duplicados
        this.locations = new HashSet<>();
        // Inicializar el mapa de datasets y el número de recursos como un HashMap
        this.datasetRes = new HashMap<>();
	}

	/**
	 * Obtiene la categoría usada en la consulta. Corresponde al valor del
	 * campo query del documento XML.
	 */
	@Override
	public String getQuery ()
	{
		return query;
	}

	/**
	 * Obtiene el número de recursos que hay en el documento XML.
	 */
	@Override
	public int getNumRes ()
	{
		return numRes;
	}

	/**
	 * Obtiene los valores del campo eventLocation de cada
	 * recurso del documento. Elimina los duplicados que pudiera haber.
	 */
	@Override
	public Collection<String> getLocations ()
	{
		return locations;
	}

	/**
	 * Obtiene la relación de identificadores de datasets,
	 * junto con el número de recursos cuyo atributo id sea
	 * igual al atributo id del dataset correspondiente.
	 */
	@Override
	public Map<String,Integer> getDatasetRes ()
	{
		return datasetRes;
	}

	/**
	 * Método que evalúa las expresiones XPath sobre un fichero XML.
	 */
	@Override
	public void evaluarXPATH() throws Exception 
	{
		/**
		 *  TODO: analizar todas las expresiones para generar:
		 *	- query
		 *	- numRes
		 *	- locations
		 *	- datasetRes
		 */
		
		// Evaluar la expresión XPath para obtener la categoría (query)
        query = (String) xPath.evaluate(XPATH_QUERY, doc, XPathConstants.STRING);

        // Evaluar la expresión XPath para obtener el número de recursos (numRes)
        numRes = ((Double) xPath.evaluate(XPATH_NUM_RESOURCES, doc, XPathConstants.NUMBER)).intValue();

        // Evaluar la expresión XPath para obtener las ubicaciones de eventos (locations)
        //Obtener todos los nodos eventLocation
        NodeList locationNodes = (NodeList) xPath.evaluate(XPATH_LOCATIONS, doc, XPathConstants.NODESET);
        for (int i = 0; i < locationNodes.getLength(); i++) {
            String location = locationNodes.item(i).getNodeValue();
         // Itera a través de cada nodo en 'locationNodes'.
            // Obtiene el valor del nodo actual y lo almacena en la variable 'location'.
            
            if (!location.isEmpty()) {
                locations.add(location);
            }
        }

        // Evaluar la expresión XPath para obtener los datasets y contar los recursos asociados
        //XPathExpression exprDatasets = xPath.compile(XPATH_DATASETS);
        //Primer dataset (su titulo): String expresionPrimerDataset = "//searchResults/results/datasets/dataset[position()=1]/title";
      //Obtener todos los nodos dataset
        NodeList datasetNodes = (NodeList) xPath.evaluate(XPATH_DATASETS, doc, XPathConstants.NODESET);
        for (int i = 0; i < datasetNodes.getLength(); i++) { //Recorrer cada nodo dataset
        	 Element datasetElement = (Element) datasetNodes.item(i);
        	// Node nodoDataset = listaNodos.item(i); En nodoDataset se guardará el contenido de un elemento <dataset>
        	 //String titulo=(String) xPath.evaluate("title/text()", nodoDataset,XPathConstants.STRING); Guardar el título
        	 String datasetId = datasetElement.getAttribute("id");
          // Itera a través de cada nodo en 'datasetNodes'.
          // Convierte el nodo actual a un elemento y obtiene el valor de su atributo 'id', almacenándolo en 'datasetId'.
             
            String resourceCountXPath = String.format("count(/searchResults/results/resources/resource[@id='%s'])", datasetId);
         // Construye una expresión XPath que cuenta los nodos <resource> que tienen un atributo 'id' igual a 'datasetId'.
            /*Otra forma de hacerlo:
            String countExpression = "count(//searchResults/results/resources/resource[@id=\"" + datasetId + "\"])";
            int resourceCount = ((Number) xPath.evaluate(countExpression, doc, XPathConstants.NUMBER)).intValue();
            */
            
            int resourceCount = ((Double) xPath.evaluate(resourceCountXPath, doc, XPathConstants.NUMBER)).intValue();
            datasetRes.put(datasetId, resourceCount);
            
            
        }
		
	}
}
