/*
 * Created on 27 août 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package clavier;

/**
 * @author MOULAÏ Philippe <br>
 * 
 * 
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Cette classe récupère les touches saisies au clavier
 * Chaque méthode retourne les informations saisies au clavier
 * dans le format désiré<br>.
 * <hr>
 * Pour afficher les chiffres décimaux 2 chiffres après la virgule
 * vous pouvez utiliser <br>
 * DecimalFormat df = new DecimalFormat("##00.00");
 * <br> puis afficher<br>
 * System.out.println(df.format(maValeurEnDecimal));
 */

public class In 
{
	/*
	 * @param in 
	 * c'est une reference sur le flux du clavier
	 */
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	/**
	 * Cette methode permet de retourner les touches tapées par 
	 * l'utilisateur sous forme d'entier 
	 * @return un entier qui correspond à la valeur tapée sur le clavier 
	 * @exception Problème avec le clavier, declaration obligatoire en java
	 * @exception NumberFormatException si la valeur saisie n'est pas un entier
	 */
	
	public static int readInteger () 
	{	
		/*
		 * mot stocke les touches tapées au clavier
		 * jusqu'a ce que l'utilisateur appuie sur 
		 * la touche entrée du clavier
		 */	
		String mot= null;
		try
		{	
			mot = new String(in.readLine());
			/*
			 * Si des touches ont été appuyées
			 */
			if (mot.length() > 0)
			{
				/*
				 * La valeur entiere est retournée
				 */
				return Integer.valueOf(mot).intValue();
			}	    
		}
		catch(IOException e)
		{
			System.out.println("Erreur de lecture du clavier " + e);
		}
		catch(NumberFormatException e1)
		{
			/*
			 * Si la valeur saisie au clavier n'est pas un entier,
			 * je passe dans ce code
			 */
			System.out.println("Vous n'avez pas saisi un entier ");
		}
		return Integer.valueOf(' ').intValue();	
	}
	
	
	/**
	 * Cette methode permet de retourner les touches tapées par 
	 * l'utilisateur sous forme de double 
	 * @return un double qui correspond à la valeur tapée sur le clavier 
	 * @exception IOException : problème avec le clavier, declaration obligatoire en java
	 * @exception NumberFormatException si la valeur saisie n'est pas un double
	 */
	public static double readDouble() //throws Exception
	{
		String mot= null;
		try
		{	/*
			 * mot stocke les touches tapées au clavier
			 * jusqu'a ce que l'utilisateur appuie sur 
			 * la touche entrée du clavier
			 */	
			mot = new String(in.readLine());
			/*
			 * Si des touches ont été appuyées
			 */
			if (mot.length() > 0)
			{
				/*
				 * La valeur double est retournée
				 */
				return Double.valueOf(mot).doubleValue();
			}	    
		}
		catch(IOException e)
		{
			System.out.println("Erreur de lecture du clavier " + e);
		}
		catch(NumberFormatException e1)
		{
			/*
			 * Si la valeur saisie au clavier n'est pas un double,
			 * je passe dans ce code
			 */
			System.out.println("Vous n'avez pas saisi un entier ");
		}
		return Double.valueOf(' ').doubleValue();
	}
	
	
	/**
	 * Cette methode permet de retourner les touches tapées par 
	 * l'utilisateur sous forme de flottant 
	 * @return un flottant qui correspond à la valeur tapée sur le clavier 
	 * @exception IOException : problème avec le clavier, declaration obligatoire en java
	 * @exception NumberFormatException si la valeur saisie n'est pas un flottant
	 */
	public static float readFloat() 
	{
		try
		{
			/*
			 * mot stocke les touches tapées au clavier
			 * jusqu'a ce que l'utilisateur appuie sur 
			 * la touche entrée du clavier
			 */	
			String mot = new String (in.readLine());
			/*
			 * Si des touches ont été appuyées
			 */
			if(mot.length() > 0)
			{
				/*
				 * La valeur en flottant est retournée
				 */
				return Float.valueOf(mot).floatValue();
			}

		}
		catch(IOException e)
		{
			System.out.println("Erreur de lecture de clavier " + e);
		}
		catch(Exception e1)
		{
			 /* Si la valeur saisie au clavier n'est pas un double,
			 * je passe dans ce code
			 */
			System.out.println("Vous n'avez pas saisi un FLOAT ");
		}
		return Float.valueOf(' ').floatValue();
	}
	/**
	 * Cette methode permet de retourner les touches tapées par 
	 * l'utilisateur sous forme d'une chaine de caratère 
	 * @return une chaine de caratère qui correspond à la valeur tapée sur le clavier 
	 * @exception IOException si il y a un problème avec le clavier
	 */
	public static String readString() //throws Exception
	{
		try
		{
			/*
			 * Retourne directemet les touches tapées au clavier
			 * lorsque l'utilisateur a appuiyé sur 
			 * la touche entrée du clavier
			 */	
			return in.readLine();
		}
		catch(IOException e)
		{
			System.out.println("Erreur de lecture de clavier " + e);
		}
		return null;
	}
	
	/**
	 * Cette methode permet de retourner les touches tapées par 
	 * l'utilisateur sous forme de caratère 
	 * @return un caratère qui correspond à la valeur tapée sur le clavier 
	 * @exception IOException si il y a un problème avec le clavier
	 * @exception NumberFormatException si la valeur saisie n'est pas un caratère
	 */
	public static char readChar() //throws Exception
	{
		String mot= null;
		try
		{	/*
			 * mot stocke les touches tapées au clavier
			 * jusqu'a ce que l'utilisateur appuie sur 
			 * la touche entrée du clavier
			 */	
			mot = new String(in.readLine());
			/*
			 * Si des touches ont été appuyées
			 */
			if (mot.length() > 0)
			{
				/*
				 * La valeur de tpe char est retournée
				 */
				return mot.charAt(0);
			}	    
		}
		catch(IOException e)
		{
			System.out.println("Erreur de lecture du clavier " + e);
		}
		catch(NumberFormatException e1)
		{
			/*
			 * Si la valeur saisie au clavier n'est pas un char,
			 * je passe dans ce code
			 */
			System.out.println("Vous n'avez pas saisi un entier ");
		}
		return ' ';	
	}
	
	/**
	 * Cette methode permet de retourner les touches tapées par 
	 * l'utilisateur sous forme de byte 
	 * @return une chaine de caratère qui correspond à la valeur tapée sur le clavier 
	 * @exception IOException si il y a un problème avec le clavier
	 * @exception NumberFormatException si la valeur saisie n'est pas un byte
	 */
	public static byte readByte() //throws Exception
	{
		String mot= null;
		try
		{	/*
			 * mot stocke les touches tapées au clavier
			 * jusqu'a ce que l'utilisateur appuie sur 
			 * la touche entrée du clavier
			 */	
			mot = new String(in.readLine());
			/*
			 * Si des touches ont été appuyées
			 */
			if (mot.length() > 0)
			{
				/*
				 * La valeur byte est retournée
				 */
				return Byte.valueOf(mot).byteValue();
			}	    
		}
		catch(IOException e)
		{
			System.out.println("Erreur de lecture du clavier " + e);
		}
		catch(NumberFormatException e1)
		{
			/**
			 * Si la valeur saisie au clavier n'est pas un byte,
			 * je passe dans ce code
			 */
			System.out.println("Vous n'avez pas saisi un entier ");
		}
		return 0;	
	}
	
	/**
	 * Cette methode permet de retourner les touches tapées par 
	 * l'utilisateur sous forme d'un tableau de caractère 
	 * @return une chaine de caratère qui correspond à la valeur tapée sur le clavier 
	 * @exception IOException si il y a un problème avec le clavier
	 * @exception NumberFormatException si la valeur saisie n'est pas un tableau de caractère
	 */
	public static char[] readArrayChar()// throws Exception
	{
		String mot= null;
		try
		{	/*
			 * mot stocke les touches tapées au clavier
			 * jusqu'a ce que l'utilisateur appuie sur 
			 * la touche entrée du clavier
			 */	
			mot = new String(in.readLine());
			/*
			 * Si des touches ont été appuyées
			 */
			if (mot.length() > 0)
			{
				/*
				 * La valeur est de type char[] est retournée
				 */
				return mot.toCharArray();
			}	    
		}
		catch(IOException e)
		{
			System.out.println("Erreur de lecture du clavier " + e);
		}
		catch(NumberFormatException e1)
		{
			/*
			 * Si la valeur saisie au clavier n'est pas un byte,
			 * je passe dans ce code
			 */
			System.out.println("Vous n'avez pas saisi un entier ");
		}
		return null;
	}


}
