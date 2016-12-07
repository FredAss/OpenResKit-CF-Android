package openreskit.odata;

import java.sql.SQLException;
import java.util.UUID;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


/**
 * Klasse zur Regelung des Zugriff auf die SQLite-Datenbank des Smartphones mittels
 * ORMLite 
 *
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "footprint.db";
	private static final int DATABASE_VERSION = 83;
	
	private Dao<Footprint, UUID> footprintDao;
	private Dao<FootprintPosition, UUID> footprintPositionDao;
	private Dao<Flight, UUID> flightsDao;	
	private Dao<Car, UUID> carsDao;
	private Dao<Distance, UUID> distanceDao;
	private Dao<PublicTransport, UUID> publicTransportDao;
	private Dao<EnergyConsumption, UUID> energyConsumptionDao;
	Dao <Employee, UUID> employeeDao;
	
	public DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * Erstellt eine Tabelle vom Typ der Datenklasse.
	 */
	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) { //Create method for ORM database tables which is called when DatabaseHelper is started
		try 
		{
			TableUtils.createTable(connectionSource, Footprint.class);
			TableUtils.createTable(connectionSource, FootprintPosition.class);
			TableUtils.createTable(connectionSource, Flight.class);			
			TableUtils.createTable(connectionSource, Car.class);			
			TableUtils.createTable(connectionSource, Distance.class);
			TableUtils.createTable(connectionSource, EnergyConsumption.class);
			TableUtils.createTable(connectionSource, PublicTransport.class);
			TableUtils.createTable(connectionSource, Employee.class);
		}
		catch (SQLException e)
		{
			Log.e(DatabaseHelper.class.getName(), "Unable to create database", e);
		}

	}

	/**
	 * Löscht die Tabelle einer bestimmten Datenklasse.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer,
			int newVer) {
		try 
		{
			TableUtils.dropTable(connectionSource, Footprint.class, true);
			TableUtils.dropTable(connectionSource, FootprintPosition.class, true);
			TableUtils.dropTable(connectionSource, Flight.class, true);			
			TableUtils.dropTable(connectionSource, Car.class, true);			
			TableUtils.dropTable(connectionSource, Distance.class, true);
			TableUtils.dropTable(connectionSource, EnergyConsumption.class, true);
			TableUtils.dropTable(connectionSource, PublicTransport.class, true);
			TableUtils.dropTable(connectionSource, Employee.class, true);

			onCreate(sqliteDatabase, connectionSource);
			
		}
		catch (SQLException e) 
		{
			Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
					+ newVer, e);
		}

	}
	/**
	 * Liefert alle Footprints im DAO Format.
	 * @return footprintDao
	 * @throws SQLException
	 */
	public Dao<Footprint, UUID> getFootprintDao() throws SQLException 
	{ 
		if (footprintDao == null) 
		{
			footprintDao = getDao(Footprint.class);
		}
		return footprintDao;
	}
	/**
	 * Liefert alle Footprint-Positionen im DAO Format.
	 * @return footprintPositionDao
	 * @throws SQLException
	 */
	public Dao<FootprintPosition, UUID> getFootprintPositionDao() throws SQLException 
	{ 
		if (footprintPositionDao == null) 
		{
			footprintPositionDao = getDao(FootprintPosition.class);
		}
		return footprintPositionDao;
	}	
	/**
	 * Liefert alle Flüge im DAO Format.
	 * @return flightsDao
	 * @throws SQLException
	 */
	public Dao<Flight, UUID> getFlightsDao() throws SQLException 
	{ 
		if (flightsDao == null) 
		{
			flightsDao = getDao(Flight.class);
		}
		return flightsDao;
	}
	/**
	 * Liefert alle Autofahrten im DAO Format.
	 * @return carsDao
	 * @throws SQLException
	 */
	public Dao<Car, UUID> getCarDao() throws SQLException 
	{ 
		if (carsDao == null) 
		{
			carsDao = getDao(Car.class);
		}
		return carsDao;
	}
	/**
	 * Liefert alle Distanzen im DAO Format.
	 * @return distanceDao
	 * @throws SQLException
	 */
	public Dao<Distance, UUID> getDistanceDao() throws SQLException 
	{ 
		if (distanceDao == null) 
		{
			distanceDao = getDao(Distance.class);
		}
		return distanceDao;
	}
	/**
	 * Liefert alle Energieverbräuche im DAO Format.
	 * @return energyConsumptionDao
	 * @throws SQLException
	 */
	public Dao<EnergyConsumption, UUID> getEnergyConsumptionDao() throws SQLException 
	{ 
		if (energyConsumptionDao == null) 
		{
			energyConsumptionDao = getDao(EnergyConsumption.class);
		}
		return energyConsumptionDao;
	}
	/**
	 * Liefert alle Fahrten in öffentlichen Verkehrsmitteln im DAO Format.
	 * @return publicTransportDao
	 * @throws SQLException
	 */
	public Dao<PublicTransport, UUID> getPublicTransportDao() throws SQLException 
	{ 
		if (publicTransportDao == null) 
		{
			publicTransportDao = getDao(PublicTransport.class);
		}
		return publicTransportDao;
	}	
	public Dao<Employee, UUID> getEmployeeDao() throws SQLException 
	{ 
		if (employeeDao == null) 
		{
			employeeDao = getDao(Employee.class);
		}
		return employeeDao;
	}	
}
