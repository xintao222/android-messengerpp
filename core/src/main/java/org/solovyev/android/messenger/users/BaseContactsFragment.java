/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.users;

import android.content.Context;
import android.support.v4.app.Fragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.BaseAsyncListFragment;
import org.solovyev.android.messenger.SyncRefreshListener;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.common.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.UiEventType.new_contact;

public abstract class BaseContactsFragment extends BaseAsyncListFragment<UiContact, ContactListItem> implements DetachableFragment {

	@Nonnull
	private static String TAG = "ContactsFragment";

	public BaseContactsFragment() {
		super(TAG, R.string.mpp_contacts, true, true);
	}

	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Override
	protected boolean canReuseFragment(@Nonnull Fragment fragment, @Nonnull ContactListItem selectedItem) {
		if (fragment instanceof BaseUserFragment) {
			final User user = ((BaseUserFragment) fragment).getUser();
			return user != null && selectedItem.getContact().equals(user);
		}
		return false;
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return new SyncRefreshListener(SyncTask.user_contacts);
	}

	@Override
	protected void onEvent(@Nonnull AccountEvent event) {
		super.onEvent(event);
		switch (event.getType()) {
			case state_changed:
				postReload();
				break;
		}
	}


	/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Nullable
	@Override
	protected Builder<ActivityMenu<Menu, MenuItem>> newMenuBuilder() {
		return new MenuBuilder();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			final MenuItem item = menu.getItem(i);
			if (item.getItemId() == R.id.mpp_menu_add_contact) {
				item.setVisible(getAccountService().canCreateUsers());
			}
		}
		super.onPrepareOptionsMenu(menu);
	}

	private final class AddContactMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_add_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
			getEventManager().fire(new_contact.newEvent());
		}
	}

	private class MenuBuilder implements Builder<ActivityMenu<Menu, MenuItem>> {
		@Nonnull
		@Override
		public ActivityMenu<Menu, MenuItem> build() {
			final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();
			menuItems.add(new ToggleFilterInputMenuItem(BaseContactsFragment.this));
			menuItems.add(new AddContactMenuItem());
			return ListActivityMenu.fromResource(R.menu.mpp_menu_contacts, menuItems, SherlockMenuHelper.getInstance());
		}
	}
}
