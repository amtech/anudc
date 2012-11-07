package au.edu.anu.datacommons.db;

public class DaoException extends Exception
{

	public DaoException()
	{
		super();
	}

	public DaoException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DaoException(String message)
	{
		super(message);
	}

	public DaoException(Throwable cause)
	{
		super(cause);
	}

}
