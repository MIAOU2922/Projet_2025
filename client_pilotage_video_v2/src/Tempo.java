

/**
 * WarningMsg.java Parts of Robot
 * @author Grassi Wilfrid
 * @version: 1.0.0
 * @date: 29/10/2018
 * 
 */

public class Tempo
{
	public Tempo(int delai)
	{
		try 
		{
			Thread.sleep(delai);
		}
		catch (InterruptedException e1) 
		{

		}
	}
}
