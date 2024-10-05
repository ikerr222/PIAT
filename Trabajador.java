package piat.opendatasearch;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Iker Bermejo Lurueña Iker.bluruena@alumnos.upm.es
 */


public class Trabajador implements Runnable{

	//Atributos
	private final String id;
	private final Collection<String> con;
	private final Collection<Map<String,String>> res;
	private final AtomicInteger cont;
	private final String nombre;
	
	public Trabajador(String id, Collection<String> con, Collection<Map<String,String>> res, AtomicInteger cont){
		this.id = id;
		this.con = con;
		this.res = res;
		this.cont = cont;
		
	    this.nombre = "Trabajador que procesa el URL " + id;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Thread.currentThread().setName(nombre);
		try {
			procesarFichero ( id );
			cont.incrementAndGet();
		}catch( Exception e){
			System.err.println(nombre + ": " + e.getMessage() );
		}
	}

	private final void procesarFichero( String id ) throws Exception {
		// TODO Auto-generated method stub
		final AnalizadorJSON aJSON = new AnalizadorJSON (id, con);
		aJSON.analizarRecursos();
		final Collection<Map<String,String>> parciales = aJSON.getRecursos();
		synchronized ( res ) {
			res.addAll(parciales);
		}
	}
	
	
}
