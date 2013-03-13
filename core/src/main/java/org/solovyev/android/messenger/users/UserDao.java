package org.solovyev.android.messenger.users;

import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:12 PM
 */

public interface UserDao {

    /*
    **********************************************************************
    *
    *                           USERS
    *
    **********************************************************************
    */

    /**
     * Method save user with his properties to persistence storage
     * Note: this method doesn't check if user with same ID is already in the storage.
     *
     * @param user user to be inserted in persistence storage
     * @return newly inserted user
     */
    @Nonnull
    User insertUser(@Nonnull User user);

    /**
     * Method loads user by if from storage
     *
     * @param userId user id
     * @return user previously saved into storage identified by <var>userId</var>, null if no such user exists in storage
     */
    @Nullable
    User loadUserById(@Nonnull String userId);

    /**
     * Method loads user properties
     *
     * @param userId user id
     * @return properties of a user previously saved into storage, empty list if no such user exists in storage or no properties are set for him
     */
    @Nonnull
    List<AProperty> loadUserPropertiesById(@Nonnull String userId);

    /**
     * Method updates user and his properties in the storage
     * @param user user to be save in the storage, nothing is done if user is not yet in the storage
     */
    void updateUser(@Nonnull User user);

    /**
     * @return all ids of users saved in the storage
     */
    @Nonnull
    List<String> loadUserIds();

    /**
     * Method deletes all users and their properties from the storage
     */
    void deleteAllUsers();

    /**
     * Method deletes all users from realm identified by realm id
     * @param realmId realm id
     */
    void deleteAllUsersInRealm(@Nonnull String realmId);

    /*
    **********************************************************************
    *
    *                           CONTACTS
    *
    **********************************************************************
    */

    /**
     * @param userId id of a user for which list of contacts should be returned
     * @return list of ids of contacts of a user identified by user id
     */
    @Nonnull
    List<String> loadUserContactIds(@Nonnull String userId);

    /**
     *
     * @param userId id of a user for which list of contacts should be returned
     * @return list of contacts of a user identified by user id
     */
    @Nonnull
    List<User> loadUserContacts(@Nonnull String userId);

    /**
     * Method merges passed user <var>contacts</var> with contacts stored in the storage.
     * The result of an operation might be adding, removal, updating of user contacts.
     *
     * @param userId id of a user for which merge should be done
     * @param contacts list of ALL contacts of a user
     * @param allowRemoval allow contacts removal
     * @param allowUpdate allow contacts update
     *
     * @return merge result
     *
     * @see org.solovyev.android.messenger.MergeDaoResult
     */
    @Nonnull
    MergeDaoResult<User, String> mergeUserContacts(@Nonnull String userId,
                                                   @Nonnull List<User> contacts,
                                                   boolean allowRemoval,
                                                   boolean allowUpdate);
}
