package org.oregami.test;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oregami.data.GameDao;
import org.oregami.data.GameEntryTypeDao;
import org.oregami.data.GameTitleDao;
import org.oregami.data.TitleTypeDao;
import org.oregami.dropwizard.OregamiService;
import org.oregami.entities.Game;
import org.oregami.entities.GameTitle;
import org.oregami.entities.GameToGameTitleConnection;
import org.oregami.entities.ReleaseGroup;
import org.oregami.entities.datalist.GameEntryType;
import org.oregami.entities.datalist.ReleaseType;
import org.oregami.entities.datalist.TitleType;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

public class PersistenceTest {

	private static Injector injector;
	
	EntityManager entityManager = null;
	
	public PersistenceTest() {
	}
	
	@BeforeClass
	public static void init() {
		JpaPersistModule jpaPersistModule = new JpaPersistModule(OregamiService.JPA_UNIT);
		injector = Guice.createInjector(jpaPersistModule);
		injector.getInstance(PersistenceTest.class);
		PersistService persistService = injector.getInstance(PersistService.class);
		persistService.start();
	}
	
	@Before
	public void startTx() {
		if (entityManager==null) {
			entityManager = injector.getInstance(EntityManager.class);
		}
		entityManager.getTransaction().begin();
		
		//deletes are not necessary, because of the Rollback after every Test
//		DatabaseFiller.getInstance().deleteGameData();
//		DatabaseFiller.getInstance().deleteBaseListData();
	}
	
	@After
	public void rollbackTx() {
		entityManager.getTransaction().rollback();
	}
	
	private <T> T getInstance(Class<T> c) {
		return injector.getInstance(c);
	}
	
	@Test
	public void testSaveGame() {
		Game game = new Game();
		game.connectGameTitle(new GameTitle("The Secret of Monkey Island"), new TitleType(TitleType.MAIN_TITLE));
		
		GameDao gameDao = getInstance(GameDao.class);
		Long gameId = gameDao.save(game);
		Assert.assertNotNull(gameId);
		
		Game loadedGame = gameDao.findOne(gameId);
		Assert.assertNotNull(loadedGame);
		Assert.assertEquals(game.getGameEntryType(), loadedGame.getGameEntryType());
	}
	
	@Test
	public void testSaveGameEntryType() {
		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);
		gameEntryTypeDao.save(gameEntryType);
		
		GameEntryType loadedGameEntryType = gameEntryTypeDao.findOne(gameEntryType.getId());
		Assert.assertNotNull(loadedGameEntryType);
		Assert.assertEquals(loadedGameEntryType.getValue(), gameEntryType.getValue());
	}
	
	
	
	@Test
	public void testSaveMultipleGameEntryTypes() {
		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);

		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
		gameEntryTypeDao.save(gameEntryType);
		
		GameEntryType gameEntryType2 = new GameEntryType(GameEntryType.COMPILATION);
		gameEntryTypeDao.save(gameEntryType2);
		
		GameEntryType gameEntryType3 = new GameEntryType(GameEntryType.ADD_ON_SIGNIFICANT);
		gameEntryTypeDao.save(gameEntryType3);
		
		List<GameEntryType> all = gameEntryTypeDao.findAll();
		Assert.assertNotNull(all);
		Assert.assertEquals(all.size(), 3);
	}	
	
	
	/**
	 * if a Game is deleted, does the associated GameEntryType stay in the database?
	 * This works because the Game.GameEntryType field does NOT have CascadeType.ALL.
	 */
	@Test
	public void testSaveAndDeleteGameWithMultipleGameEntryTypes() {
		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);
		GameDao gameDao = getInstance(GameDao.class);
		GameTitleDao gameTitleDao = getInstance(GameTitleDao.class);
		
		GameTitle gameTitle = new GameTitle("The Secret of Monkey Island");
		gameTitleDao.save(gameTitle);
		
		TitleTypeDao titleTypeDao = getInstance(TitleTypeDao.class);
		TitleType titleType = new TitleType(TitleType.MAIN_TITLE);
		titleTypeDao.save(titleType);
		
		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
		gameEntryTypeDao.save(gameEntryType);
		
		GameEntryType gameEntryType2 = new GameEntryType(GameEntryType.COMPILATION);
		gameEntryTypeDao.save(gameEntryType2);
		
		GameEntryType gameEntryType3 = new GameEntryType(GameEntryType.ADD_ON_SIGNIFICANT);
		gameEntryTypeDao.save(gameEntryType3);
		
		Game game = new Game();
		game.connectGameTitle(gameTitle, titleType);
		game.setGameEntryType(gameEntryType);
		Long gameId = gameDao.save(game);
		Assert.assertNotNull(gameId);
		
		List<GameEntryType> allGameEntryTypes = gameEntryTypeDao.findAll();
		Assert.assertNotNull(allGameEntryTypes);
		Assert.assertEquals(allGameEntryTypes.size(), 3);
		
		List<Game> allGames = gameDao.findAll();
		Assert.assertNotNull(allGames);
		Assert.assertEquals(allGames.size(), 1);
		
		gameDao.delete(game);
		
		allGames = gameDao.findAll();
		Assert.assertNotNull(allGames);
		Assert.assertEquals(allGames.size(), 0);
		
		allGameEntryTypes = gameEntryTypeDao.findAll();
		Assert.assertNotNull(allGameEntryTypes);
		Assert.assertEquals(allGameEntryTypes.size(), 3);		
		
	}		
	
	@Test
	public void testSaveGameWithGameEntryType() {
		
		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);
		gameEntryTypeDao.save(gameEntryType);
		
		GameEntryType loadedGameEntryType = gameEntryTypeDao.findOne(gameEntryType.getId());
		Assert.assertNotNull(loadedGameEntryType);
		Assert.assertEquals(loadedGameEntryType.getValue(), gameEntryType.getValue());
		
		GameDao gameDao = getInstance(GameDao.class);
		
		Game game = new Game();
		game.setGameEntryType(gameEntryType);
		Long gameId = gameDao.save(game);
		Assert.assertNotNull(gameId);

		Game game2 = new Game();
		game2.setGameEntryType(gameEntryType);
		Long gameId2 = gameDao.save(game2);
		Assert.assertNotNull(gameId2);
		
		Game loadedGame = gameDao.findOne(gameId);
		Assert.assertNotNull(loadedGame);
		Assert.assertEquals(game.getGameEntryType(), loadedGame.getGameEntryType());

		Game loadedGame2 = gameDao.findOne(gameId2);
		Assert.assertNotNull(loadedGame2);
		Assert.assertEquals(game2.getGameEntryType(), loadedGame2.getGameEntryType());
		
		List<Game> allGames = gameDao.findAll();
		Assert.assertEquals(2, allGames.size());
		
		
	}
	
	
	@Test
	public void testSaveReleaseGroup() {
		
		ReleaseType releaseType = new ReleaseType(ReleaseType.NATIVE_DEVELOPMENT);
		entityManager.persist(releaseType);
		
		ReleaseGroup releaseGroup = new ReleaseGroup();
		releaseGroup.setReleaseType(releaseType);
		entityManager.persist(releaseGroup);
		Long id = releaseGroup.getId();
		Assert.assertNotNull(id);
		
		ReleaseGroup rgLoaded = entityManager.find(ReleaseGroup.class, id);
		Assert.assertNotNull(rgLoaded);
		Assert.assertEquals(rgLoaded.getReleaseType(), releaseGroup.getReleaseType());
		System.out.println(rgLoaded);
		
	}
	
	@Test
	public void testSaveGameTitle() {
		GameTitleDao titleDao = getInstance(GameTitleDao.class);
		GameTitle title = new GameTitle("The Secret of Monkey Island");
		titleDao.save(title);
		
		Assert.assertEquals(titleDao.findAll().size(), 1);
		
		GameTitle title2 = new GameTitle("Le Secret de L'Ile aux Singes");
		titleDao.save(title2);
		
		Assert.assertEquals(titleDao.findAll().size(), 2);
		
	}
	
	
	@Test
	public void testSaveGameWithGameTitle() {
		GameTitleDao titleDao = getInstance(GameTitleDao.class);
		GameTitle title = new GameTitle("The Secret of Monkey Island");
		titleDao.save(title);
		GameTitle title2 = new GameTitle("Monkey Island");
		titleDao.save(title2);
		Assert.assertEquals(titleDao.findAll().size(), 2);
		
		TitleTypeDao titleTypeDao = getInstance(TitleTypeDao.class);
		TitleType titleType = new TitleType(TitleType.MAIN_TITLE);
		TitleType titleType2 = new TitleType(TitleType.ABBREVIATION);
		titleTypeDao.save(titleType);
		titleTypeDao.save(titleType2);
		
		Assert.assertEquals(titleTypeDao.findAll().size(), 2);

		GameDao gameDao = getInstance(GameDao.class);
		Game game = new Game();
		game.connectGameTitle(title, titleType);
		game.connectGameTitle(title2, titleType2);
		Long gameId = gameDao.save(game);
		
		Game gameLoaded = gameDao.findOne(gameId);
		Set<GameToGameTitleConnection> connectionList = gameLoaded.getGameToGameTitleConnectionList();
		Assert.assertEquals(connectionList.size(), 2);
		
		GameToGameTitleConnection gameTitleConnectionLoaded = connectionList.iterator().next();
		Assert.assertNotNull(gameTitleConnectionLoaded);
		
		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
		
		//delete Game an test if GameTitle objects stay in the database
		gameDao.delete(game);
		List<Game> allGames = gameDao.findAll();
		Assert.assertNotNull(allGames);
		Assert.assertEquals(allGames.size(), 0);
		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
		
	}
	
	@Test
	public void testSaveGameWithGameTitle2() {
		GameTitleDao titleDao = getInstance(GameTitleDao.class);
		GameTitle title = new GameTitle("The Secret of Monkey Island");
		titleDao.save(title);
		GameTitle title2 = new GameTitle("Monkey Island");
		titleDao.save(title2);
		Assert.assertEquals(titleDao.findAll().size(), 2);
		
		TitleTypeDao titleTypeDao = getInstance(TitleTypeDao.class);
		TitleType titleType = new TitleType(TitleType.MAIN_TITLE);
		TitleType titleType2 = new TitleType(TitleType.ABBREVIATION);
		titleTypeDao.save(titleType);
		titleTypeDao.save(titleType2);
		
		Assert.assertEquals(titleTypeDao.findAll().size(), 2);

		GameDao gameDao = getInstance(GameDao.class);
		Game game = new Game();
		game.connectGameTitle(title, titleType);
		game.connectGameTitle(title2, titleType2);
		Long gameId = gameDao.save(game);
		
		Game gameLoaded = gameDao.findOne(gameId);
		Set<GameToGameTitleConnection> connectionList = gameLoaded.getGameToGameTitleConnectionList();
		Assert.assertEquals(connectionList.size(), 2);
		
		GameToGameTitleConnection gameTitleConnectionLoaded = connectionList.iterator().next();
		Assert.assertNotNull(gameTitleConnectionLoaded);
		//Assert.assertEquals(gameTitleConnectionLoaded.getTitleType(), titleType2);
		
		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
		
	}	
	
}
