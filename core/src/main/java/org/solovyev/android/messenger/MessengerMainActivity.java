package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatGuiEvent;
import org.solovyev.android.messenger.chats.ChatGuiEventType;
import org.solovyev.android.messenger.messages.MessengerEmptyFragment;
import org.solovyev.android.messenger.messages.MessengerMessagesActivity;
import org.solovyev.android.messenger.messages.MessengerMessagesFragment;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.UnsupportedRealmException;
import org.solovyev.android.messenger.users.*;
import org.solovyev.common.Builder;
import roboguice.event.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:52 PM
 */
public class MessengerMainActivity extends MessengerFragmentActivity implements EventListener {

    @NotNull
    private static final String MESSAGES_PANE_TAG = "messages_pane";

    @NotNull
    private static final String CONTACT_PANE_TAG = "contact_pane";

    public MessengerMainActivity() {
        super(R.layout.msg_main);
    }

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerMainActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getEventManager().registerObserver(ContactGuiEvent.class, this);
        getEventManager().registerObserver(ChatGuiEvent.class, this);

        if (isDualPane()) {
            createPaneFragment(MESSAGES_PANE_TAG, R.id.content_second_pane);
        }

        if (isTriplePane()) {
            createPaneFragment(CONTACT_PANE_TAG, R.id.content_third_pane);
        }
    }

    private void createPaneFragment(@NotNull String tag, int parentViewId) {
        final FragmentManager fm = getSupportFragmentManager();

        Fragment messagesFragment = fm.findFragmentByTag(tag);

        final FragmentTransaction ft = fm.beginTransaction();
        try {
            if (messagesFragment == null) {
                messagesFragment = Fragment.instantiate(this, MessengerEmptyFragment.class.getName(), null);
                ft.add(parentViewId, messagesFragment, tag);
            } else {
                if (messagesFragment.isDetached()) {
                    ft.attach(messagesFragment);
                }
            }
        } finally {
            ft.commit();
        }
    }

    @Override
    public void onEvent(Object event) {
        if (event instanceof ContactGuiEvent) {
            handleContactGuiEvent((ContactGuiEvent) event);
        } else if (event instanceof ChatGuiEvent) {
            handleChatGuiEvent((ChatGuiEvent) event);
        }
    }

    private void handleChatGuiEvent(@NotNull ChatGuiEvent event) {
        final Chat chat = event.getChat();
        final ChatGuiEventType type = event.getType();

        if (type == ChatGuiEventType.chat_clicked) {
            if (isDualPane()) {
                replaceFragment(MESSAGES_PANE_TAG, R.id.content_second_pane, new Builder<Fragment>() {
                    @NotNull
                    @Override
                    public Fragment build() {
                        return new MessengerMessagesFragment(chat);
                    }
                });

                if ( isTriplePane() ) {
                    if (chat.isPrivate()) {
                        replaceFragment(CONTACT_PANE_TAG, R.id.content_third_pane, new Builder<Fragment>() {
                            @NotNull
                            @Override
                            public Fragment build() {
                                return new MessengerContactFragment(chat.getSecondUserId());
                            }
                        });
                    } else {
                        replaceFragment(CONTACT_PANE_TAG, R.id.content_third_pane, new Builder<Fragment>() {
                            @NotNull
                            @Override
                            public Fragment build() {
                                final List<User> participants = new ArrayList<User>();
                                for (Realm realm : getRealmService().getRealms()) {
                                    final User user = realm.getUser();
                                    participants.addAll(getChatService().getParticipantsExcept(chat.getId(), user.getId()));

                                }
                                return new MessengerContactsInfoFragment(participants);
                            }
                        });
                    }
                }

            } else {
                MessengerMessagesActivity.startActivity(this, chat);
            }
        }
    }

    private void replaceFragment(@NotNull String tag,
                                 int parentViewId,
                                 @NotNull Builder<Fragment> builder) {
        final FragmentManager fm = getSupportFragmentManager();

        final Fragment oldFragment = fm.findFragmentByTag(tag);
        final FragmentTransaction ft = fm.beginTransaction();

        try {
            final Fragment newMessagesFragment;

            if (oldFragment instanceof MessengerEmptyFragment) {
                if (oldFragment.isAdded()) {
                    ft.remove(oldFragment);
                }

                newMessagesFragment = builder.build();
            } else if (oldFragment instanceof MessengerMessagesFragment) {
                final MessengerMessagesFragment oldMessagesFragment = (MessengerMessagesFragment) oldFragment;

                /*if (chat.equals(oldMessagesFragment.getChat())) {
                    // same fragment
                    if (oldMessagesFragment.isDetached()) {
                        ft.attach(oldMessagesFragment);
                    }

                    newMessagesFragment = null;

                } else {*/
                    // another fragment
                    if (oldMessagesFragment.isAdded()) {
                        ft.remove(oldMessagesFragment);
                    }

                    newMessagesFragment = builder.build();
               /* }*/
            } else {
                if (oldFragment != null && oldFragment.isAdded()) {
                    ft.remove(oldFragment);
                }

                newMessagesFragment = builder.build();
            }

            if (newMessagesFragment != null) {
                ft.add(parentViewId, newMessagesFragment, tag);
            }
        } finally {
            ft.commit();
        }
    }

    private void handleContactGuiEvent(@NotNull ContactGuiEvent event) {
        final User contact = event.getContact();
        final ContactGuiEventType type = event.getType();

        if (type == ContactGuiEventType.contact_clicked) {

            new AsyncTask<Void, Void, Chat>() {

                @Override
                protected Chat doInBackground(Void... params) {
                    try {
                        final User user = getRealmService().getRealmById(contact.getRealmUser().getRealmId()).getUser();
                        return AbstractMessengerApplication.getServiceLocator().getUserService().getPrivateChat(user.getId(), contact.getId(), MessengerMainActivity.this);
                    } catch (UnsupportedRealmException e) {
                        throw new AssertionError(e);
                    }
                }

                @Override
                protected void onPostExecute(@NotNull Chat chat) {
                    super.onPostExecute(chat);

                    getEventManager().fire(ChatGuiEventType.newChatClicked(chat));
                }

            }.execute(null, null);

            if ( isTriplePane() ) {
                replaceFragment(CONTACT_PANE_TAG, R.id.content_third_pane, new Builder<Fragment>() {
                    @NotNull
                    @Override
                    public Fragment build() {
                        return new MessengerContactFragment(contact);
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getEventManager().unregisterObserver(ChatGuiEvent.class, this);
        getEventManager().unregisterObserver(ContactGuiEvent.class, this);
    }
}