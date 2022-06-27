package com.ebabu.event365live.host.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebabu.event365live.host.DI.App;
import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentSelectEventBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class SelectEventFragment extends Fragment {
    FragmentSelectEventBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_event, container, false);

        binding.privateEvent.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_selectEventFragment_to_eventCategoryFragment, null));

        binding.privateEvent.setOnClickListener(v -> {

            CreateEventDAO createEventDAO;
            if (App.createEventDAO != null) {
                createEventDAO = App.createEventDAO;
            } else {
                createEventDAO = new CreateEventDAO();
            }

            createEventDAO.setEventType(1);
            Navigation.findNavController(v)
                    .navigate(SelectEventFragmentDirections.actionSelectEventFragmentToEventCategoryFragment(createEventDAO));
        });

        binding.openEvent.setOnClickListener(v -> {
            CreateEventDAO createEventDAO;
            if (App.createEventDAO != null) {
                createEventDAO = App.createEventDAO;
            } else {
                createEventDAO = new CreateEventDAO();
            }
            createEventDAO.setEventType(0);

            Navigation.findNavController(v)
                    .navigate(SelectEventFragmentDirections.actionSelectEventFragmentToEventCategoryFragment(createEventDAO));
        });

        binding.backArrow.setOnClickListener(v -> getActivity().onBackPressed());
        return binding.getRoot();
    }

}
