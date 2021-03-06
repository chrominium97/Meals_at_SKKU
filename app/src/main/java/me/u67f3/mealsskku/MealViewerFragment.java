package me.u67f3.mealsskku;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A placeholder fragment containing a simple view.
 */
public class MealViewerFragment extends Fragment {

    public class Meal {
        final String title;
        final String type;
        final String description;

        String getName() {
            return this.title;
        }

        String getType() {
            return this.type;
        }

        String getDescription() {
            return this.description;
        }

        Meal(String title, String type, String description) {
            this.title = title;
            this.type = type;
            this.description = description;
        }
    }

    private static final String TAG = "MealViewerFragment";

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_DATE = "date";
    private View rootView;
    private RecyclerView mealListView;

    private final ArrayList<Object> objects = new ArrayList<>();

    public MealViewerFragment() {

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MealViewerFragment newInstance(int sectionNumber, String date) {
        Log.d("Fragment", "Initialized fragment at position " + sectionNumber);
        MealViewerFragment fragment = new MealViewerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SimpleDateFormat")
    public void updateData() {
        updateData(new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }

    @SuppressLint("SimpleDateFormat")
    public void updateData(String date) {

        rootView.findViewById(R.id.progess_circle).setVisibility(View.VISIBLE);
        mealListView.setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.error_dialog).setVisibility(View.INVISIBLE);

        try {
            new SimpleDateFormat("yyyyMMdd").parse(date);
        } catch (ParseException e) {
            Log.w(TAG, "updateData: Invalid date string passed. Fallback to current date", e);
        }

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadMealsTask().execute(
                    "http://skku-meals.azurewebsites.net/?" +
                            "cafeteria=" + getActivity().getResources().getIntArray(R.array.cafeteria_codes)[getArguments().getInt(ARG_SECTION_NUMBER)] +
                            "&date=" + date
            );
        } else {
            Log.w(TAG, "updateData: No internet connection...");

            rootView.findViewById(R.id.progess_circle).setVisibility(View.GONE);
            ((TextView) rootView.findViewById(R.id.section_label))
                    .setText(getString(R.string.no_internet));
            rootView.findViewById(R.id.error_dialog).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mealListView = (RecyclerView) rootView.findViewById(R.id.meal_list);
        mealListView.setHasFixedSize(true);
        mealListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        String date = getArguments().getString(ARG_DATE);
        if (date == null) {
            updateData();
        } else {
            updateData(date);
        }
        mealListView.setAdapter(adapter);
        return rootView;
    }

    final RecyclerView.Adapter adapter = new RecyclerView.Adapter() {

        final int TYPE_CARD_VIEW = 1;
        final int TYPE_SEPARATOR_VIEW = 2;

        class CardViewHolder extends RecyclerView.ViewHolder {
            TextView mealName;
            TextView mealType;
            TextView mealDesc;

            CardViewHolder(View view) {
                super(view);
                mealName = (TextView) view.findViewById(R.id.meal_name);
                mealType = (TextView) view.findViewById(R.id.meal_type);
                mealDesc = (TextView) view.findViewById(R.id.meal_desc);
            }
        }

        class SeparatorViewHolder extends RecyclerView.ViewHolder {
            TextView typeDesc;

            SeparatorViewHolder(View view) {
                super(view);
                typeDesc = (TextView) view.findViewById(R.id.type_desc);
            }
        }

        @Override
        @SuppressLint("InflateParams")
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_CARD_VIEW:
                    return new CardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cards, null));
                case TYPE_SEPARATOR_VIEW:
                    return new SeparatorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.separator, null));
                default:
                    return null;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (objects.size() == 0) {
                return TYPE_SEPARATOR_VIEW;
            } else if (objects.get(position).getClass().equals(Meal.class)) {
                return TYPE_CARD_VIEW;
            } else {
                return TYPE_SEPARATOR_VIEW;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            switch (getItemViewType(position)) {

                case TYPE_CARD_VIEW:
                    CardViewHolder card = (CardViewHolder) holder;

                    final Meal meal = (Meal) objects.get(position);
                    card.mealName.setText(meal.getName());
                    card.mealType.setText(meal.getType());
                    card.mealDesc.setText(meal.getDescription());
                    break;

                case TYPE_SEPARATOR_VIEW:
                    SeparatorViewHolder separator = (SeparatorViewHolder) holder;

                    String title;
                    if (objects.size() == 0) {
                        title = getString(R.string.no_meal);
                    } else {
                        title = (String) objects.get(position);
                        switch (title) {
                            case "breakfast":
                                title = getString(R.string.breakfast);
                                break;
                            case "lunch":
                                title = getString(R.string.lunch);
                                break;
                            case "dinner":
                                title = getString(R.string.dinner);
                                break;
                            case "etc":
                                title = getString(R.string.etc);
                                break;
                        }
                    }
                    separator.typeDesc.setText(title);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            int size = objects.size();
            if (size != 0) {
                return size;
            }
            return 1;
        }
    };

    private class DownloadMealsTask extends AsyncTask<String, Void, Map<String, ArrayList<Meal>>> {
        @Override
        protected Map<String, ArrayList<Meal>> doInBackground(String... params) {

            URL url;
            if (params.length != 1) {
                return null;
            } else {
                try {
                    url = new URL(params[0]);
                } catch (Exception e) {
                    return null;
                }
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.flush();

                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                int nLength;
                while ((nLength = inputStream.read(byteBuffer, 0, byteBuffer.length)) != -1) {
                    byteOutputStream.write(byteBuffer, 0, nLength);
                }
                String response = new String(byteOutputStream.toByteArray());
                JSONObject array = new JSONObject(response);
                Iterator<String> keys = array.keys();

                Map<String, ArrayList<Meal>> output = new HashMap<>();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONArray mealArray = array.getJSONArray(key);
                    ArrayList<Meal> meals = new ArrayList<>();
                    for (int i = 0; i < mealArray.length(); i++) {
                        JSONObject mealObject = mealArray.getJSONObject(i);
                        meals.add(new Meal(
                                mealObject.getString("name"),
                                mealObject.getString("price") + " ₩",
                                mealObject.getString("description")
                        ));
                    }
                    output.put(key, meals);
                }

                return output;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Map<String, ArrayList<Meal>> result) {

            Comparator<String> comparator = new Comparator<String>() {
                public int compare(String o1, String o2) {
                    if (o1.equals(o2)) {
                        return 0;
                    }

                    String[] strings = {o1, o2};
                    int[] values = {3, 3};
                    for (int i = 0; i < strings.length; i++) {
                        switch (strings[i]) {
                            case "breakfast":
                                values[i] = 0;
                                break;
                            case "lunch":
                                values[i] = 1;
                                break;
                            case "dinner":
                                values[i] = 2;
                                break;
                        }
                    }
                    if (values[0] == -1 && values[1] == -1) {
                        Arrays.sort(strings);
                        if (strings[0].equals(o1)) {
                            return -1;
                        }
                        return 1;
                    } else {
                        return values[0] - values[1];
                    }

                }
            };
            SortedSet<String> keys = new TreeSet<>(comparator);
            keys.addAll(result.keySet());

            Iterator<String> iterator = keys.iterator();
            ArrayList<Object> output = objects;
            objects.clear();
            while (iterator.hasNext()) {
                String key = iterator.next();
                output.add(key);
                output.addAll(result.get(key));
            }
            rootView.findViewById(R.id.progess_circle).setVisibility(View.GONE);
            mealListView.setVisibility(View.VISIBLE);
            mealListView.getAdapter().notifyDataSetChanged();
        }
    }
}
