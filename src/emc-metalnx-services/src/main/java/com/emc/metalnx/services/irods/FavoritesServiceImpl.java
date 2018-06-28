 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.FavoriteDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserFavorite;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FavoritesService;
import com.emc.metalnx.services.interfaces.IRODSServices;

@Service("favoritesService")
@Transactional
public class FavoritesServiceImpl implements FavoritesService {

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	CollectionService collectionService;

	@Autowired
	FavoriteDao favoriteDao;

	private static final Logger logger = LoggerFactory.getLogger(FavoritesServiceImpl.class);

	/**
	 * Updates the favorites table for a user, whether be it to remove or add a path
	 *
	 * @param user
	 * @param set
	 *            of paths to be added
	 * @param set
	 *            of paths to be removed
	 * @return True, if operation is successful. False, otherwise.
	 */
	@Override
	public boolean updateFavorites(DataGridUser user, Set<String> toAdd, Set<String> toRemove) {

		boolean operationResult = true;

		try {
			if (toAdd != null) {
				for (String path : toAdd) {
					if (!findFavoritesForUserAsString(user).contains(path)) {
						favoriteDao.addByUserAndPath(user, path, collectionService.isCollection(path));
					}
				}
			}

			if (toRemove != null) {
				for (String path : toRemove) {
					favoriteDao.removeByUserAndPath(user, path);
				}
			}
		} catch (Exception e) {
			operationResult = false;
			logger.error("Could not modify favorite for {}", user.getUsername(), e);
		}

		return operationResult;
	}

	/**
	 * Returns a list of strings with each of them representing a path marked as
	 * favorite
	 *
	 * @param user
	 * @return List of paths marked as favorites by the user.
	 */
	@Override
	public List<String> findFavoritesForUserAsString(DataGridUser user) {
		List<DataGridUserFavorite> favorites = favoriteDao.findByUser(user);
		List<String> strings = new ArrayList<String>();

		for (DataGridUserFavorite favorite : favorites) {
			strings.add(favorite.getPath());
		}

		return strings;
	}

	@Override
	public List<DataGridUserFavorite> findFavoritesPaginated(DataGridUser user, int offset, int limit,
			String searchString, String orderBy, String orderDir, boolean onlyCollections) {
		List<DataGridUserFavorite> favorites = favoriteDao.findByUserPaginated(user, offset, limit, searchString,
				orderBy, orderDir, onlyCollections);
		return favorites;
	}

	/**
	 * Removes path from database. This operation is used when the corresponding
	 * collection or file is deleted from the grid
	 *
	 * @param path
	 * @return True, if operation is successful. False, otherwise.
	 */
	@Override
	public boolean removeFavoriteBasedOnPath(String path) {
		return favoriteDao.removeByPath(path);
	}

	/**
	 * Removes path from database. This operation is used when the corresponding
	 * collection or file is deleted from the grid
	 *
	 * @param path
	 * @return True, if operation is successful. False, otherwise.
	 */
	@Override
	public boolean removeFavoriteBasedOnRelativePath(String path) {
		return favoriteDao.removeByParentPath(path);
	}

	/**
	 * Checks whether the parameter path is a favorite for parameter user
	 *
	 * @param user
	 * @param path
	 * @return True, if path is a favorite for user. False, otherwise.
	 */
	@Override
	public boolean isPathFavoriteForUser(DataGridUser user, String path) {
		return favoriteDao.findByUserAndPath(user, path) != null;
	}

	@Override
	public boolean removeFavoriteBasedOnUser(DataGridUser user) {
		return favoriteDao.removeByUser(user);
	}

}
