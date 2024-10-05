
package piat.opendatasearch;

import java.util.Map;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

/**
 * @author Iker Bermejo Lurueña iker.bluruena@alumnos.upm.es
/**
 * Clase que implementa el analizador XML usando un SAXParser.
 * Extiende la clase DefaultHandler para que pueda ser usada por un SAXParser,
 * por lo que tiene que sobreescribir sus métodos públicos.
 * Implementa la interfaz ParserCatálogo para que pueda ser usada por la
 * aplicación para obtener la información de concepts y datasets pertinentes.
 */
public final class AnalizadorXML
extends DefaultHandler
implements ParserCatálogo
{
	// Analizador SAX que se usará para analizar el catálogo
	private final SAXParser saxParser;
	// Categoría que se usará para seleccionar la información pertinente
	private final String cat;
	// Stream para leer el catálogo
	private final InputStream doc;
	// Colección en la que anotar los concepts pertinentes a devolver
	private final Collection<String> concepts;
	// Mapa en el que registrar los datasets pertinentes a devolver
	private final Map<String,Map<String,String>> datasets;

	// TODO: añadir los campos que se necesiten

	private StringBuilder sb; //StringBuilder que se utiliza para acumular los caracteres de los elementos XML durante el análisis.
    private boolean categoriaEncontrada; //Se utiliza para verificar si se ha encontrado la categoría solicitada durante el análisis del XML 
	
	//Conceptos
	private String category; // Es una variable que almacena el valor del elemento <category> del XML, utilizado para comparar con la categoría solicitada
	private String IdConcept; // Id del concepto
	private String IdConceptPedido; // Es una variable que almacena el ID del concepto solicitado para comprobar posibles subcategorías.
	
	
	//Datasets
	private String IdDataset; //Id del dataset
	private HashMap<String, String> datasetActual; // Es un mapa que almacena los atributos del dataset actual durante el análisis del XML
	private String title;
	private String description;
	private String theme;
	private boolean comprobacion; // Se utiliza para comprobar si el dataset actual está asociado con un concepto pertinente.
	/**
	 * Constructor de la clase.
	 * @param cat la categoría que se usará para seleccionar la
	 * información pertinente.
	 * @param doc el documento XML que contiene el catálogo a analizar.
	 */
	public AnalizadorXML ( String cat, InputStream doc )
	throws ParserConfigurationException, SAXException
	{
		// TODO: codificar el constructor
		this.cat = cat;
        this.doc = doc;
        this.concepts = new ArrayList<>();
        this.datasets = new HashMap<>();
        
        this.IdConcept = null;
        this.IdConceptPedido = null;
        this.IdDataset = null;

        
        sb = new StringBuilder();
        categoriaEncontrada=false;
        comprobacion=false;
       
        
        // Crear el analizador SAX
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        this.saxParser = factory.newSAXParser();
        
	}

	/**
	 * Analiza el contenido del catálogo y obtiene la información
	 * pertinente. Debe ser invocado una sola vez.
	 */
	public void analizar ()
	throws SAXException, IOException
	{
		// TODO: código adicional si se considera necesario
		saxParser.parse ( doc, this );
	}

	//===========================================================
	// Métodos a implementar de la interfaz ParserCatálogo
	//===========================================================

	@Override
	public Collection<String> getConcepts ()
	{
		// TODO: código adicional si se considera necesario
		return concepts;
	}

	@Override
	public Map<String,Map<String,String>> getDatasets ()
	{
		// TODO: código adicional si se considera necesario
		return datasets;
	}

	//===========================================================
	// Métodos a implementar de SAX DocumentHandler
	//===========================================================

	@Override
	public final void startDocument() throws SAXException
	{
		super.startDocument();
		// TODO: código adicional si se considera necesario
		System.out.println("Comienza el documento");
	}
	
	@Override
	public final void endDocument() throws SAXException
	{
		super.endDocument();
		// TODO: código adicional si se considera necesario
		System.out.println("Fin del documento");
	}

	@Override
	public final void startElement ( //Inicio de Elemento XML
		String nsURI,
		String localName,
		String qName,
		Attributes atts )
		throws SAXException
	{
		super.startElement ( nsURI, localName, qName, atts );
		// TODO: código adicional si se considera necesario
		
		 switch (localName) {
		 
         case "concept": //Conceptos
        	 
        	IdConcept = atts.getValue("id");
        	 
             break;
             
         case "dataset": //Datasets
        	 
        	IdDataset = atts.getValue("id");
 			comprobacion = false;
 			datasetActual = new LinkedHashMap<String, String>();
             
             break;
             
         case "refConcept": //Comprobar RefConcepts
        	 
        	 final String refConcept = atts.getValue("id");
             for (String s : concepts) {
                 if (s.equals(refConcept))
                     comprobacion = true;
             }
             //Comprobamos si el refConcept hace referencia a un concept pertinente
             
             sb.setLength(0); //Aseguramos que el StringBuilder esté vacío antes de comenzar a acumular nuevos caracteres.
//             Es necesario asegurarse de que no contenga caracteres de análisis 
//             anteriores cuando se analiza un nuevo elemento XML.
             break;
         default:
             break;
     }

	}

	@Override
	public final void endElement (
		String uri,
		String localName,
		String qName )
		throws SAXException
	{
		super.endElement ( uri, localName, qName );
		// TODO: código adicional si se considera necesario
		
		switch(localName) {
		
		//Conceptos
		
		case "category":
			
			category = sb.toString().trim(); // Obtenemos el valor de la categoria
			
			if(category.equals(cat) && categoriaEncontrada == false) { 
		// Si el codigo del elemento es igual al que nos han pedido en el constructor, analizamos.
				IdConceptPedido = IdConcept; //Id actual = Id pedido
				concepts.add(IdConcept); // Guardamos el valor del Id
				categoriaEncontrada = true;
				
			} // Cuando encontramos la categoría pedida (Argumento) 
			
			
			if(categoriaEncontrada == true) {
				
				if(IdConcept.contains(IdConceptPedido) && !IdConcept.equals(IdConceptPedido)) {
					concepts.add(IdConcept); // Guardamos el valor del ID 
				}
//				 Si ya se ha encontrado una categoría válida, se verifica 
//				 si el ID del concepto actual (IdConcept) contiene el ID del 
//				 concepto pedido (IdConceptPedido) y si no son iguales. 
//				 Si se cumple, se agrega el ID del concepto actual a la colección de conceptos.
			}
			 sb.setLength(0); //Reiniciamos
		 
		break;
			
		case "label":
			sb.setLength(0);
			break;
		
		//Datasets
		
         case "title":
			title = sb.toString().trim();
			 sb.setLength(0);
			break;
		
         case "description":
			description = sb.toString().trim();
			 sb.setLength(0);
			break;
	
         case "keyword":
        	 sb.setLength(0);
			break;
				
         case "theme":
			theme = sb.toString().trim();
			 sb.setLength(0);
			break;
			
         case "publisher":
        	 sb.setLength(0);
        	break;
		
		
         case "dataset":
	
        	 if(comprobacion) { // Dataset actual está asociado con un concepto pertinente.
			datasetActual.put("title", title);
			datasetActual.put("description", description);
			datasetActual.put("theme", theme);
			
		    datasets.put(IdDataset, datasetActual);
		}
        	 
			comprobacion = false;
			break;
         default:
             break;
		}
		
		
	}

				
	@Override
	public final void characters (
		char chars[],
		int start,
		int length )
		throws SAXException
	{
		super.characters ( chars, start, length );
		// TODO: código adicional si se considera necesario
		sb.append(chars,start,length);
	}
}
