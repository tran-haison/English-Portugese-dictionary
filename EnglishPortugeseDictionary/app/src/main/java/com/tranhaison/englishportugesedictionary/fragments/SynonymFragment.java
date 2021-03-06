package com.tranhaison.englishportugesedictionary.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tranhaison.englishportugesedictionary.utils.Constants;
import com.tranhaison.englishportugesedictionary.network.NetworkUtil;
import com.tranhaison.englishportugesedictionary.utils.texttospeech.GoogleTextToSpeech;
import com.tranhaison.englishportugesedictionary.R;
import com.tranhaison.englishportugesedictionary.utils.texttospeech.LocalTextToSpeech;
import com.tranhaison.englishportugesedictionary.adapters.detailactivity.fragment.SynonymAdapter;
import com.tranhaison.englishportugesedictionary.databases.DatabaseHelper;
import com.tranhaison.englishportugesedictionary.dictionaryhelper.words.DictionaryWord;
import com.tranhaison.englishportugesedictionary.interfaces.FragmentListener;
import com.tranhaison.englishportugesedictionary.interfaces.ListItemListener;
import com.tranhaison.englishportugesedictionary.interfaces.Updatable;

import java.util.ArrayList;

public class SynonymFragment extends Fragment implements Updatable {

    // Init View
    LinearLayout linearLayoutSynonymPrompt;
    ListView listViewSynonym;

    // Init text to speech
    LocalTextToSpeech localTextToSpeech;
    GoogleTextToSpeech googleTextToSpeech;

    // Init global variables
    private DictionaryWord dictionaryWord;
    private DatabaseHelper databaseHelper;
    private int dictionary_type;

    // Init adapter and array
    private SynonymAdapter synonymAdapter;
    private ArrayList<DictionaryWord> relatedWordList;

    // Init fragment listener
    private FragmentListener fragmentListener;

    public SynonymFragment(DatabaseHelper databaseHelper, DictionaryWord dictionaryWord, int dictionary_type,
                           LocalTextToSpeech localTextToSpeech, GoogleTextToSpeech googleTextToSpeech) {
        this.databaseHelper = databaseHelper;
        this.dictionaryWord = dictionaryWord;
        this.dictionary_type = dictionary_type;
        this.localTextToSpeech = localTextToSpeech;
        this.googleTextToSpeech = googleTextToSpeech;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_synonym, container, false);

        // Map View
        linearLayoutSynonymPrompt = view.findViewById(R.id.linearLayoutSynonymPrompt);
        listViewSynonym = view.findViewById(R.id.listViewSynonym);

        // Init array list
        relatedWordList = new ArrayList<>();

        // Get list of related words
        getRelatedWordList();
        setRelatedWordList();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Get list of related words depending on dictionary_type
     * @return
     */
    private void getRelatedWordList() {
        if (relatedWordList != null) {
            relatedWordList.clear();
        }

        if (dictionary_type == Constants.ENG_POR) {
            relatedWordList = databaseHelper.getPorRelatedWord(dictionaryWord.getWordList_id());
        } else if (dictionary_type == Constants.POR_ENG) {
            relatedWordList = databaseHelper.getEngRelatedWord(dictionaryWord.getWordList_id());
        }
    }

    /**
     * Check to see if a word has its related words or not
     * @return
     */
    private boolean hasRelatedWord() {
        if (relatedWordList != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set related word list to list view
     */
    private void setRelatedWordList() {
        // If a word has other related words -> Show in list view and handle event for each related word
        // else display prompt
        if (hasRelatedWord()) {
            // Set adapter to list view
            synonymAdapter = new SynonymAdapter(getActivity(), relatedWordList);
            try {
                listViewSynonym.setAdapter(synonymAdapter);

                // Override the event handler from adapter
                synonymAdapter.setOnItemClick(new ListItemListener() {
                    @Override
                    public void onItemClick(int position) {
                        if (fragmentListener != null) {
                            fragmentListener.onItemClick(relatedWordList.get(position).getDisplayWord());
                        }
                    }
                });

                // Override the event handler from adapter
                synonymAdapter.setOnItemSpeakerClick(new ListItemListener() {
                    @Override
                    public void onItemClick(int position) {
                        String displayWord = relatedWordList.get(position).getDisplayWord();
                        if (dictionary_type == Constants.ENG_POR) {
                            if (NetworkUtil.isNetworkConnected(getContext())) {
                                googleTextToSpeech.play(displayWord, Constants.CODE_PORTUGUESE);
                            } else {
                                localTextToSpeech.speakPortuguese(displayWord);
                            }
                        } else if (dictionary_type == Constants.POR_ENG) {
                            if (NetworkUtil.isNetworkConnected(getContext())) {
                                googleTextToSpeech.play(displayWord, Constants.CODE_ENGLISH);
                            } else {
                                localTextToSpeech.speakEnglish(displayWord, Constants.CODE_US);
                            }
                        }
                    }
                });
            } catch (Exception e) {}
        } else {
            try {
                linearLayoutSynonymPrompt.setVisibility(View.VISIBLE);
                listViewSynonym.setVisibility(View.GONE);
            } catch (NullPointerException e) {}
        }
    }

    /**
     * Pass interface instance as parameter
     * @param fragmentListener
     */
    public void setOnFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    @Override
    public void update(int dictionary_type, int wordList_id) {
        this.dictionary_type = dictionary_type;
        this.dictionaryWord = databaseHelper.getWord(wordList_id, dictionary_type);

        getRelatedWordList();
        setRelatedWordList();
    }
}