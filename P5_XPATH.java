package piat.opendatasearch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;


public class P5_XPATH
{
	
	//@author Iker Bermejo Lurueña iker.bluruena@alumnos.upm.es
	
	/**
	 * Clase principal de la aplicación de extracción de información del 
	 * Portal de Datos Abiertos del Ayuntamiento de Madrid
	 */
	private static final Class<?> bogus = new Object(){}.getClass();
	private static final Class<?> esta = bogus.getEnclosingClass();
	private static final String nombre = esta.getCanonicalName();
	private static final String USO = "uso: " + nombre + " <categoría> " +
		"<catálogo> <esqema_catálogo> <salida> <esquema_salida>\n\n" +
		"\t<categoría>: identificador de la categoría a buscar\n" +
		"\t<catálogo>: nombre del fichero que contiene el catálogo\n" +
		"\t<esquema_catálogo>: nombre del fichero que contiene el esquema " +
		"que debe satisfacer el fichero de entrada\n" +
		"\t<salida> nombre del fichero de salida\n" +
		"\t<esquema>: nombre del fichero que contiene el esquema " +
		"que debe satisfacer e l fichero de salida\n";

	public static void main ( String[] args ) throws Exception
	{
		try
		{
			trabajar ( args );
		}
		catch ( Exception e )
		{
			System.err.println ( e.getMessage() );
			e.printStackTrace(); // durante la depuración
		}
	}

	private static void trabajar ( String[] args ) throws Exception
	{
		if ( args.length != 6 ) {
			throw new IllegalArgumentException ( USO );
		}else {
			System.out.println("Número de argumentos correcto.");
		}
		//1. validar los argumentos
		final String categoría = args[0];
        final String catálogo = args[1];
        final String esquema_catálogo = args[2];
        final String salida = args[3];
        final String esquema = args[4];
        final String salidaJSON = args[5];
        
		//Expresiones regulares
		Pattern patronCode = Pattern.compile("^\\d{3,4}(-[A-Z0-9]{3,8})?$");
		Matcher matcherCode = patronCode.matcher(categoría);
		if(matcherCode.find() == false) {
			throw new IllegalArgumentException("Error: El codigo de la categoría es incorrecto "+ USO);
		}else {
			System.out.println("Categoría correcta.");
		}
		
		
		Pattern patronDocumento = Pattern.compile("\\.xml$");
		Matcher matcherEntrada = patronDocumento.matcher(catálogo);
		if(matcherEntrada.find() == false) {
			throw new IllegalArgumentException("Error: La ruta del fichero de entrada (ficheroCatalogo) no es correcta "+ USO);
		}else {
			System.out.println("Ruta de Catalogo.xml correcta.");
		}
		
		Matcher matcherSalida = patronDocumento.matcher(salida);
		if(matcherSalida.find() == false) {
			throw new IllegalArgumentException("Error: La ruta del fichero de salida no es correcta "+ USO);
		}else {
			System.out.println("Ruta del fichero de salida correcta.");
		}
		
		Pattern patronDocumento2 = Pattern.compile("\\.xsd$");
		Matcher matcherXsd = patronDocumento2.matcher(esquema);
		if(matcherXsd.find() == false) {
			throw new IllegalArgumentException("Error: La ruta del fichero de entrada xsd no es correcta "+ USO);
		}else {
			System.out.println("Ruta de catalogo.xsd correcta.");
		}
		
		Matcher matcherXsd2 = patronDocumento2.matcher(esquema_catálogo);
		if(matcherXsd2.find() == false) {
			throw new IllegalArgumentException("Error: La ruta del fichero de salida xsd no es correcta "+ USO);
		}else {
			System.out.println("Ruta del esquema de salida correcta.");
		}
		
		//Permiso de lectura
		File entrada = new File(catálogo);
        File entrada2 = new File(esquema_catálogo);
        File entrada3 = new File(esquema);
		if(!entrada.isFile()|| !entrada.canRead()) {
			throw new IllegalArgumentException("Error: el fichero de entrada (catálogo.xml) no existe o bien no se puede leer" + USO);
		}else {
			System.out.println("Catalogo.xml tiene permiso de lectura.");
		}
		if(!entrada2.isFile()|| !entrada2.canRead()) {
			throw new IllegalArgumentException("Error: el fichero de entrada (catálogo.xsd) no existe o bien no se puede leer" + USO);
		}else {
			System.out.println("Catalogo.xsd tiene permiso de lectura.");
		}
		if(!entrada3.isFile()|| !entrada3.canRead()) {
			throw new IllegalArgumentException("Error: el fichero de entrada (esquema de salida) no existe o bien no se puede leer" + USO);
		}else {
			System.out.println("El esquema de salida tiene permiso de lectura.");
		}
		
		//Permiso de escritura
		File salidaFile = new File(salida);
		if (!salidaFile.isFile() || !salidaFile.canWrite()) {
		    throw new IllegalArgumentException("Error: el fichero de salida no existe o no se puede escribir" + USO);
		}else {
			System.out.println("El fichero de salida tiene permiso de escritura.");
		}
	/*
		// Verificar que es posible crear el fichero de salida
		try (PrintWriter fSalida = new PrintWriter(new FileWriter(salida, true))) {
		    System.out.println("El fichero de salida puede ser creado y tiene permiso de escritura.");
		} catch (IOException e) {
		    throw new IllegalArgumentException("Error: el fichero de salida '" + salida + "' no es posible crearlo. Motivo: " + e.getMessage());
		}
		*/
		//2. validar el catálogo frente al esquema correspondiente
		
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new File(esquema_catálogo));
			
			Validator validator = schema.newValidator();
		    validator.validate(new StreamSource(new File(catálogo)));
		    System.out.println("El documento XML del catálogo es válido respecto a su XSD.");
		} catch (Exception e) {
		    System.err.println("Error al validar el documento XML contra el esquema:");
		    System.err.println(e.getMessage());
		    throw e; 
		}
		
		// 3.Procesar el catálogo usando un objeto AnalizadorXML
		
		// Crear una instancia de AnalizadorXML pasando los argumentos necesarios
		AnalizadorXML analizador = new AnalizadorXML(categoría, new FileInputStream(catálogo));

		// Realizar el análisis del catálogo
		analizador.analizar();
		
		// 4.Recoger la información pertinente del AnalizadorXML
		
		Collection<String> concepts = analizador.getConcepts();
		Map<String, Map<String, String>> datasets = analizador.getDatasets();
		
		//P4. // Procesar cada documento JSON concurrentemente utilizando hilos
		
	    Collection<Map<String, String>> recursos = new ArrayList<Map<String,String>>();
	    recursos = extraerJSON(datasets, concepts);
	    
	    /*
	     * AtomicInteger contador = new AtomicInteger(0);
	    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	     * 
	    int numThreads = Runtime.getRuntime().availableProcessors();
 
	    for (String datasetId : datasets.keySet()) {
	        Trabajador trabajador = new Trabajador(datasetId, concepts, recursos, contador, "Trabajador-" + datasetId);
	        executor.execute(trabajador);
	    }
	    executor.shutdown();
	    while (!executor.isTerminated()) {
	        // Espera a que todos los hilos terminen
	    }
		*/
		
		// 5. Generar el documento XML de salida por medio de GenerarXML
		String salidaXML = GenerarXML.generar(categoría, concepts, datasets, recursos);
		Reader readerSalida = new StringReader ( salidaXML );
		
		// 6. validar el documento XML generado frente al esquema
		// Validar el documento XML generado frente al esquema
	   try {
	    	String esqLan = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		    SchemaFactory factoría = SchemaFactory.newInstance ( esqLan );
			Source schema = new StreamSource ( esquema );
			Schema Fuenteesquema = factoría.newSchema ( schema );
	        Validator validator = Fuenteesquema.newValidator();
	        Source fuenteXML = new StreamSource ( readerSalida );
	        validator.validate(fuenteXML);
	        
          System.out.println("El documento de salida XML generado es válido según el esquema.");
	    } catch (Exception e) {
	        System.err.println("Error al validar el documento XML generado contra el esquema:" + USO);
	        System.err.println(e.getMessage());
	        throw e;
	    } 
	    
		// 7. Escribir el documento XML generado en el fichero de salida
	 
	    FileWriter ficheroXML = new FileWriter(salida);
		//Volcar los datos en el fichero de salida
		PrintWriter writer = new PrintWriter(ficheroXML, true);
		
		writer.println(salidaXML);
		writer.close();
		// Se recomienda el uso de métodos privados auxiliares
	
		
		//8.  Búsqueda de información y generación del documento JSON de resultados. Esta 
		//es la nueva funcionalidad de esta práctica y se describe en el siguiente apartado.
		
		try (FileInputStream fileInputStream = new FileInputStream(salida)) {
            AnalizadorXPATH analizadorXPath = new AnalizadorXPATH(new InputSource(fileInputStream));
            analizadorXPath.evaluarXPATH();
            GenerarJSON.generar(salidaJSON, analizadorXPath.getQuery() , analizadorXPath.getNumRes(), analizadorXPath.getLocations(), analizadorXPath.getDatasetRes());
        }
}
	
	// TODO: métodos privados auxiliares que se consideren necesarios
    
	private static Collection<Map<String, String>> extraerJSON (Map<String, Map<String, String>> datasets, Collection<String> concepts) {
		
		Collection<Map<String,String>> recursos = new HashSet<Map<String,String>>();
		//Crea una nueva Collection de tipo HashSet para almacenar los recursos extraídos.
		final int nNucleos = Runtime.getRuntime().availableProcessors(); 
		//Obtiene el número de núcleos disponibles en el procesador y lo almacena en nNucleos.
		final ExecutorService ejecutor = Executors.newFixedThreadPool(nNucleos); 
		// Crea un ExecutorService con un pool de hilos fijos cuyo tamaño es igual al número de núcleos del procesador.
		//Esto se usa para ejecutar tareas concurrentemente.
		AtomicInteger cont = new AtomicInteger(0);
		//Crea un AtomicInteger llamado cont inicializado a 0. Este contador atómico se utilizará para 
		//contar los recursos procesados de manera segura en un entorno concurrente.
		
		boolean exit = false;
		
		for (String dataset : datasets.keySet()) {
			// Crear un Trabajador para cada dataset
			ejecutor.execute(new Trabajador(dataset, concepts, recursos, cont));
		}
		// Esperar a que todas las tareas completen
		while (!exit){
			if (cont.get() == datasets.size()){
				exit = true;
			}
		}
		System.out.println("Recursos procesados: " + cont.get());
		// Apagar el ejecutor
		ejecutor.shutdown();
		
		return recursos;
	}
	
	
}