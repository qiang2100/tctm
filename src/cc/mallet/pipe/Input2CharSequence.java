/*
 * Copyright (c) 2014. Seonggyu Lee. All Rights Reserved.
 * User: Seonggyu Lee
 * Date: 14. 9. 30 오후 6:22
 * Created Date : $today.year.month.day
 * Last Modified : 14. 9. 30 오후 6:22
 * User email: shalomeir@gmail.com
 */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package cc.mallet.pipe;

import java.io.*;
import java.net.URI;

import cc.mallet.types.Instance;
import cc.mallet.util.CharSequenceLexer;

/**
 *  Pipe that can read from various kinds of text sources
 *   (either URI, File, or Reader) into a CharSequence
 *
 *  @version $Id: Input2CharSequence.java,v 1.1 2007/10/22 21:37:39 mccallum Exp $
 */
public class Input2CharSequence extends Pipe implements Serializable
{
	String encoding = null;

	public Input2CharSequence ()
	{
	}

	public Input2CharSequence( String encoding ) {
		this.encoding = encoding;
	}

	public Instance pipe (Instance carrier)
	{
		try {
			if (carrier.getData() instanceof URI)
				carrier.setData(pipe ((URI)carrier.getData()));
			else if (carrier.getData() instanceof File)
				carrier.setData(pipe ((File)carrier.getData()));
			else if (carrier.getData() instanceof Reader)
				carrier.setData(pipe ((Reader)carrier.getData()));
			else if (carrier.getData() instanceof CharSequence)
				;																	// No conversion necessary
			else
				throw new IllegalArgumentException ("Does not handle class "+carrier.getData().getClass());

		} catch (IOException e) {
			throw new IllegalArgumentException ("IOException " + e);
		}

//		System.out.println(carrier.getData().toString());
		return carrier;
	}

	public CharSequence pipe (URI uri)
		throws FileNotFoundException, IOException
	{
		if (! uri.getScheme().equals("file"))
			throw new UnsupportedOperationException ("Only file: scheme implemented.");
		return pipe (new File (uri.getPath()));
	}
	
	public CharSequence pipe (File file)
		throws FileNotFoundException, IOException
	{
		BufferedReader br = null;

		if (encoding == null) {
			br = new BufferedReader (new FileReader (file));
		}
		else {
			br = new BufferedReader( new InputStreamReader(new FileInputStream(file), encoding) );
		}

		CharSequence cs = pipe(br);
		br.close();
		return cs;
	}

	public CharSequence pipe (Reader reader)
		throws IOException
	{
		final int BUFSIZE = 2048;
		char[] buf = new char[BUFSIZE];
		int count;
		StringBuffer sb = new StringBuffer (BUFSIZE);
		do {
			count = reader.read (buf, 0, BUFSIZE);
			if (count == -1)
				break;
			//System.out.println ("count="+count);
			sb.append (buf, 0, count);
		} while (count == BUFSIZE);
		return sb;
	}

	public CharSequence pipe (CharSequence cs)
	{
		return cs;
	}

	// Serialization 
	
	private static final long serialVersionUID = 2;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt (CURRENT_SERIAL_VERSION);
		if (encoding == null) {
			out.writeObject("null");
		}
		else {
			out.writeObject(encoding);
		}
	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
		this.encoding = (String) in.readObject();
		if (encoding.equals("null")) { encoding = null; } 
	}

}
