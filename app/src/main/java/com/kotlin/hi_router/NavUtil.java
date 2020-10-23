package com.kotlin.hi_router;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.DialogFragmentNavigator;
import androidx.navigation.fragment.FragmentNavigator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kotlin.hi_router.model.BottomBar;
import com.kotlin.hi_router.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * create by max at 2020/10/23 11:51
 */

public class NavUtil {

    private static HashMap<String, Destination> destinations;

    public static String parseFile(Context context, String fileName) {
        AssetManager assets = context.getAssets();
        try {
            InputStream inputStream = assets.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuilder builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            inputStream.close();
            reader.close();


            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void buildNavGraph(FragmentActivity activity, FragmentManager childFragmentManager, NavController controller, int containerId) {
        String content = parseFile(activity, "destination.json");
        destinations = JSON.parseObject(content, new TypeReference<HashMap<String, Destination>>() {
        }.getType());

        NavigatorProvider navigatorProvider = controller.getNavigatorProvider();
        NavGraphNavigator graphNavigator = navigatorProvider.getNavigator(NavGraphNavigator.class);
        NavGraph navGraph = new NavGraph(graphNavigator);

        HiFragmentNavigator hiFragmentNavigator = new HiFragmentNavigator(activity, childFragmentManager, containerId);
        navigatorProvider.addNavigator(hiFragmentNavigator);

        Iterator<Destination> iterator = destinations.values().iterator();
        while (iterator.hasNext()) {
            Destination destination = iterator.next();
            if (destination.desTtype.equals("activity")) {
                ActivityNavigator navigator = navigatorProvider.getNavigator(ActivityNavigator.class);
                ActivityNavigator.Destination node = navigator.createDestination();
                node.setId(destination.id);
                node.setComponentName(new ComponentName(activity.getPackageName(), destination.clazName));
                navGraph.addDestination(node);
            } else if (destination.desTtype.equals("fragment")) {
                //FragmentNavigator navigator = navigatorProvider.getNavigator(FragmentNavigator.class);
                //FragmentNavigator.Destination node = navigator.createDestination();
                HiFragmentNavigator.Destination node = hiFragmentNavigator.createDestination();

                node.setId(destination.id);
                node.setClassName(destination.clazName);
                navGraph.addDestination(node);
            } else if (destination.desTtype.equals("dialog")) {
                DialogFragmentNavigator navigator = navigatorProvider.getNavigator(DialogFragmentNavigator.class);
                DialogFragmentNavigator.Destination node = navigator.createDestination();
                node.setId(destination.id);
                node.setClassName(destination.clazName);
                navGraph.addDestination(node);
            }
            if (destination.asStarter) {
                navGraph.setStartDestination(destination.id);
            }
        }
        controller.setGraph(navGraph);
    }

    public static void buildBootomBar(BottomNavigationView navView) {
        String content = parseFile(navView.getContext(), "main_tabs_config.json");
        BottomBar bottomBar = JSON.parseObject(content, BottomBar.class);

        List<BottomBar.Tab> tabs = bottomBar.tabs;
        Menu menu = navView.getMenu();
        for (BottomBar.Tab tab : tabs) {
            if (!tab.enable) continue;
            Destination destination = destinations.get(tab.pageUrl);
            if (destination != null) {
                MenuItem menuItem = menu.add(0, destination.id, tab.index, tab.title);
                menuItem.setIcon(R.drawable.ic_home_black_24dp);
            }
        }
    }
}
