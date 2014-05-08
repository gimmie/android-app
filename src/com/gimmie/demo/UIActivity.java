package com.gimmie.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.gimmie.BaseResult;
import com.gimmie.CombineResponse;
import com.gimmie.Gimmie;
import com.gimmie.RemoteCollection;
import com.gimmie.model.Checkin;
import com.gimmie.model.Event;
import com.gimmie.model.User;

public class UIActivity extends SherlockActivity {

  private static final int PAGE_LOCK = 0;
  private static final int PAGE_MAIN = 1;

  private List<Menu> mMenus = null;
  private Gimmie mGimmie;
  private Settings mSettings;
  private BaseAdapter mAdapter;

  private Menu mLoginMenu = null;
  private Menu mEventMenuHeader = null;
  private Menu mCheckinMenu = null;

  private int mPage;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mSettings = new Settings(this);
    mGimmie = Gimmie.getInstance(this);
    mGimmie.registerForPushNotification(this);
    mGimmie.login("test2", "test2",
        "test@gimmieworld.com");

    if (mSettings.isUnlocked()) {
      showMainView();
    } else {
      showLockView();
    }
  }

  private void showLockView() {
    mPage = PAGE_LOCK;
    setContentView(R.layout.locker);

    TextView versionText = (TextView) findViewById(R.id.versionText);
    versionText.setText("Version: " + mSettings.getVersionNumber());

    final TextView errorText = (TextView) findViewById(R.id.errorText);
    final EditText passcodeField = (EditText) findViewById(R.id.passcodeField);
    final Button unlockButton = (Button) findViewById(R.id.unlockButton);
    unlockButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (passcodeField.getText().toString().equals("446643")) {
          mSettings.unlock();
          mSettings.save();
          showMainView();
        } else {
          errorText.setVisibility(View.VISIBLE);
          errorText.setText(getString(R.string.error_wrong_passcode));
        }
      }
    });
  }

  private void showMainView() {
    mPage = PAGE_MAIN;
    setContentView(R.layout.main);

    mEventMenuHeader = new Menu(getString(R.string.header_actions),
        Menu.TYPE_HEADER);
    mCheckinMenu = new Menu(getString(R.string.action_checkin),
        Menu.TYPE_ACTION, new CheckinAction(mSettings));

    LoginAction loginAction = new LoginAction(this, mSettings, mGimmie);
    mLoginMenu = new Menu(getString(R.string.action_login) + " ("
        + mSettings.getDefaultUsername() + ")", Menu.TYPE_ACTION, loginAction);
    loginAction.setLoginMenu(mLoginMenu);

    mMenus = new LinkedList<Menu>();
    Menu menus[] = new Menu[] {

        new Menu(getString(R.string.header_views), Menu.TYPE_HEADER),
        // Open Gimmie View
        new Menu(getString(R.string.action_open_gimmie_view), Menu.TYPE_ACTION,
            new ListAction() {
              public void execute(Activity activity) {
                Gimmie gimmie = Gimmie.getInstance(activity);
                gimmie.getGimmieComponents().showRewardCatalogue();
              }
            }),
        // Open Catalog
        new Menu(getString(R.string.action_open_reward_catalog),
            Menu.TYPE_ACTION, new ListAction() {

              @Override
              public void execute(Activity activity) {
                Gimmie gimmie = Gimmie.getInstance(activity);
                gimmie.getGimmieComponents().showRewardCatalogueOnly();
              }

            }),
        // Open Profile
        new Menu(getString(R.string.action_open_profile), Menu.TYPE_ACTION,
            new ListAction() {

              @Override
              public void execute(Activity activity) {
                Gimmie gimmie = Gimmie.getInstance(activity);
                gimmie.getGimmieComponents().showProfileOnly();
              }

            }),
        // Open Leaderboard
        new Menu(getString(R.string.action_open_leaderboard), Menu.TYPE_ACTION,
            new ListAction() {

              @Override
              public void execute(Activity activity) {
                Gimmie gimmie = Gimmie.getInstance(activity);
                gimmie.getGimmieComponents().showLeaderboardOnly();
              }

            }),
        mEventMenuHeader,
        mCheckinMenu,
        new Menu(getString(R.string.header_settings), Menu.TYPE_HEADER),
        // Login
        mLoginMenu,
        // Change language
        new Menu(getString(R.string.action_change_language), Menu.TYPE_ACTION,
            new ChangeLanguageAction()),
        new Menu(getString(R.string.action_game_settings), Menu.TYPE_ACTION,
            new GameSettingAction(this, mSettings)),
        new Menu(getString(R.string.action_event_settings), Menu.TYPE_ACTION,
            new EventSettingAction(mSettings)),
        new Menu(getString(R.string.app_version) + " "
            + mSettings.getVersionNumber(), Menu.TYPE_HEADER) };
    Collections.addAll(mMenus, menus);

    final Activity self = this;
    mAdapter = new BaseAdapter() {

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        Menu menu = mMenus.get(position);

        if (menu.getType().equals(Menu.TYPE_ACTION)) {
          TextView textView;
          if (convertView == null) {
            textView = new TextView(self);

            Resources resources = parent.getResources();
            int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, resources.getDisplayMetrics());
            textView.setPadding(px, px, px, px);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setOnClickListener(new OnClickListener() {

              @Override
              public void onClick(View view) {
                Integer position = (Integer) view.getTag();
                ListAction action = mMenus.get(position).getAction();
                if (action != null) {
                  action.execute(self);
                }
              }
            });
          } else {
            textView = (TextView) convertView;
          }

          textView.setText(menu.getName());
          textView.setTag(position);

          return textView;
        } else {
          TextView textView;
          if (convertView == null) {
            textView = new TextView(self);

            Resources resources = parent.getResources();
            int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, resources.getDisplayMetrics());
            textView.setPadding(px, px, px, px);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setClickable(false);
          } else {
            textView = (TextView) convertView;
          }

          textView.setText(menu.getName());
          textView.setTag(position);

          return textView;
        }

      }

      @Override
      public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
      }

      @Override
      public Object getItem(int position) {
        return mMenus.get(position);
      }

      @Override
      public int getCount() {
        return mMenus.size();
      }

      @Override
      public int getItemViewType(int position) {
        Menu menu = mMenus.get(position);
        if (menu.getType().equals(Menu.TYPE_HEADER)) {
          return 0;
        }
        return 1;
      }

      @Override
      public int getViewTypeCount() {
        return 2;
      }
    };

    ListView actions = (ListView) findViewById(R.id.actionList);
    actions.setAdapter(mAdapter);
    loginAction.setAdapter(mAdapter);
  }

  public void onResume() {
    super.onResume();

    if (mPage == PAGE_MAIN) {
      Log.d(Gimmie.LOG_TAG, "Re-register notification");
      Log.d(Gimmie.LOG_TAG, "Activity: " + this);
      // Re-register notification when screen is rotated.
      Gimmie.getInstance().updateContext(this);
      mSettings.setGimmie(mGimmie);
      mSettings.load();

      mGimmie.loadEvents(new Handler(),
          new BaseResult<RemoteCollection<Event>>() {

            @Override
            public void getResult(RemoteCollection<Event> result) {
              Event[] events = result.getCollection();
              List<Menu> menus = new ArrayList<Menu>(events.length);
              for (Event event : events) {
                Menu menu = new Menu(event.getName(), Menu.TYPE_ACTION,
                    new EventAction(event.getName()));
                menus.add(menu);
              }

              synchronized (menus) {
                int eventHeaderIndex = mMenus.indexOf(mEventMenuHeader);
                int checkinIndex = mMenus.indexOf(mCheckinMenu);

                LinkedList<Menu> removingList = new LinkedList<Menu>();
                for (int index = eventHeaderIndex + 1; index < checkinIndex; index++) {
                  removingList.add(mMenus.get(index));
                }

                mMenus.removeAll(removingList);
                mMenus.addAll(eventHeaderIndex + 1, menus);
                mAdapter.notifyDataSetChanged();
              }

            }

          });
    }

  }

  private static class EventAction implements ListAction {

    private String mEventName;

    public EventAction(String eventName) {
      mEventName = eventName;
    }

    @Override
    public void execute(Activity activity) {
      Gimmie gimmie = Gimmie.getInstance();
      gimmie.trigger(mEventName);
    }

  }

  private static class LoginAction implements ListAction {

    private Activity mActivity;
    private Settings mSettings;
    private Menu mLoginMenu;
    private BaseAdapter mAdapter;
    private final Gimmie mGimmie;

    public LoginAction(Activity activity, Settings settings, Gimmie gimmie) {
      mActivity = activity;
      mSettings = settings;
      mGimmie = gimmie;
    }

    public void setLoginMenu(Menu loginMenu) {
      mLoginMenu = loginMenu;
    }

    public void setAdapter(BaseAdapter adapter) {
      mAdapter = adapter;
    }

    @Override
    public void execute(final Activity activity) {
      LinearLayout layout = new LinearLayout(activity);
      layout.setOrientation(LinearLayout.HORIZONTAL);

      final EditText user = new EditText(activity);
      user.setHint(activity.getString(R.string.view_login_username));
      layout.addView(user, new LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT));

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder
          .setTitle(activity.getString(R.string.view_login_title))
          .setPositiveButton(activity.getString(R.string.view_dialog_ok),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  String username = user.getText().toString().trim();
                  if (username.length() > 0) {
                    String oldUsername = mSettings.getDefaultUsername();
                    mSettings.setDefaultUsername(username);
                    mSettings.save();

                    mLoginMenu.setName(mActivity
                        .getString(R.string.action_login)
                        + " ("
                        + mSettings.getDefaultUsername() + ")");
                    mAdapter.notifyDataSetChanged();

                    if (oldUsername.startsWith("guest:")) {
                      Log.d(Gimmie.LOG_TAG, String.format("Guest user: %s", oldUsername));
                      mGimmie.login(username);
                      mGimmie.transferDataFromGuestID(null, oldUsername, null);

                    }
                    //link new user with device notification reg_id
                    mGimmie.registerForPushNotification(activity);
                  }

                }
              })
          .setNegativeButton(activity.getString(R.string.view_dialog_cancel),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  // Just dismiss
                }
              }).setView(layout);
      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }

  private static class ChangeLanguageAction implements ListAction {
    @Override
    public void execute(final Activity activity) {
      final String languages[] = { "English", "日本語 (Japanese)", "ไทย (Thai)" };

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setTitle(activity.getString(R.string.view_change_language_title))
          .setItems(languages, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              Locale newLocale = null;
              switch (which) {
              case 1:
                newLocale = new Locale("ja");
                break;
              case 2:
                newLocale = new Locale("th");
                break;
              default:
                newLocale = new Locale("en", "US");
                break;
              }

              Resources res = activity.getResources();
              DisplayMetrics dm = res.getDisplayMetrics();
              Configuration conf = res.getConfiguration();
              conf.locale = newLocale;
              res.updateConfiguration(conf, dm);
              Intent refresh = new Intent(activity, UIActivity.class);
              activity.startActivity(refresh);

            }
          });

      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }

  private static class GameSettingAction implements ListAction {

    private Settings mSettings;
    private Activity mActivity;

    public GameSettingAction(Activity activity, Settings settings) {
      mSettings = settings;
      mActivity = activity;
    }

    @Override
    public void execute(Activity activity) {
      LinearLayout layout = new LinearLayout(activity);
      layout.setOrientation(LinearLayout.VERTICAL);

      final EditText keySetting = new EditText(activity);
      keySetting.setHint(activity.getString(R.string.view_settings_key));
      keySetting.setText(mSettings.getGimmieKey());
      layout.addView(keySetting, new LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT));

      final EditText secretSetting = new EditText(activity);
      secretSetting.setHint(activity.getString(R.string.view_settings_secret));
      secretSetting.setText(mSettings.getGimmieSecret());
      layout.addView(secretSetting, new LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT));

      final EditText serverSetting = new EditText(activity);
      serverSetting.setHint(activity.getString(R.string.view_settings_server));
      serverSetting.setText(mSettings.getGimmieURL());
      layout.addView(serverSetting, new LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT));

      final EditText countrySetting = new EditText(activity);
      countrySetting
          .setHint(activity.getString(R.string.view_settings_country));
      countrySetting.setText(mSettings.getGimmieCountry());
      layout.addView(countrySetting, new LayoutParams(
          LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder
          .setTitle(activity.getString(R.string.action_game_settings))
          .setPositiveButton(activity.getString(R.string.view_dialog_ok),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  String key = keySetting.getText().toString().trim();
                  if (key.length() > 0) {
                    mSettings.setGimmieKey(key);
                  }

                  String secret = secretSetting.getText().toString().trim();
                  if (secret.length() > 0) {
                    mSettings.setGimmieSecret(secret);
                  }

                  String server = serverSetting.getText().toString().trim();
                  if (server.length() > 0) {
                    mSettings.setGimmieURL(server);
                  }

                  String country = countrySetting.getText().toString().trim();
                  if (country.length() > 0) {
                    mSettings.setGimmieCountry(country);
                  }

                  mSettings.save();
                  mActivity.finish();
                  mActivity.startActivity(mActivity.getIntent());
                }
              })
          .setNegativeButton(activity.getString(R.string.view_dialog_cancel),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  // Just dismiss
                }
              }).setView(layout);

      builder.create().show();
    }
  }

  private static class CheckinAction implements ListAction {

    private Settings mSettings;

    public CheckinAction(Settings settings) {
      mSettings = settings;
    }

    @Override
    public void execute(final Activity activity) {
      LinearLayout layout = new LinearLayout(activity);
      layout.setOrientation(LinearLayout.HORIZONTAL);

      final EditText place = new EditText(activity);
      place.setHint(activity.getString(R.string.view_checkin_place));
      layout.addView(place, new LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT));

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder
          .setTitle(activity.getString(R.string.view_checkin_title))
          .setPositiveButton(activity.getString(R.string.view_checkin_confirm),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                  Gimmie gimmie = Gimmie.getInstance(activity);
                  gimmie.checkin(new Handler(activity.getMainLooper()),
                      mSettings.getCheckinID(), place.getText().toString(),
                      new BaseResult<CombineResponse>() {

                        @Override
                        public void getResult(CombineResponse result) {

                          User user = result.getSubObject(User.class,
                              CombineResponse.FIELD_USER);
                          Log.v(Gimmie.LOG_TAG, "User: " + user.getUserID());

                          Checkin checkin = result.getSubObject(Checkin.class,
                              CombineResponse.FIELD_CHECK_IN);
                          Log.v(Gimmie.LOG_TAG, checkin.raw().toString());

                        }
                      });
                }
              })
          .setNegativeButton(activity.getString(R.string.view_dialog_cancel),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  // Just dismiss
                }
              }).setView(layout);

      builder.create().show();
    }
  }

  private static class EventSettingAction implements ListAction {

    private Settings mSettings;

    public EventSettingAction(Settings settings) {
      mSettings = settings;
    }

    @Override
    public void execute(Activity activity) {
      LinearLayout layout = new LinearLayout(activity);
      layout.setOrientation(LinearLayout.VERTICAL);

      final EditText checkinEventSetting = new EditText(activity);
      checkinEventSetting.setHint(activity
          .getString(R.string.view_settings_checkin_id));
      layout.addView(checkinEventSetting, new LayoutParams(
          LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder
          .setTitle(activity.getString(R.string.action_event_settings))
          .setPositiveButton(activity.getString(R.string.view_dialog_ok),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  String checkinEventName = checkinEventSetting.getText()
                      .toString().trim();
                  if (checkinEventName.length() > 0) {
                    mSettings.setCheckinID(checkinEventName);
                    mSettings.save();
                  }
                }
              })
          .setNegativeButton(activity.getString(R.string.view_dialog_cancel),
              new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                  // Just dismiss
                }
              }).setView(layout);

      builder.create().show();
    }
  }

}
