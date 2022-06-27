package com.ebabu.event365live.host.fragments.edit_ticket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentEditTicktBinding;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.RegularTicketDao;
import com.ebabu.event365live.host.entities.RsvpVipTicketDao;
import com.ebabu.event365live.host.entities.TableSeatingTicketDao;
import com.ebabu.event365live.host.fragments.CreateEventFragmentArgs;
import com.ebabu.event365live.host.fragments.edit_ticket.adapter.EditTicketAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class EditTicktFragment extends Fragment {
    FragmentEditTicktBinding binding;
    private EditTicketAdapter editTicketAdapter;
    public static CreateEventDAO createEventDAO;
    public static List<RegularTicketDao> regularTicketList;
    public static List<RsvpVipTicketDao> rsvpVipTicketList;
    public static List<TableSeatingTicketDao> tableSeatingTicketList;
    private static View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createEventDAO = CreateEventFragmentArgs.fromBundle(getArguments()).getEventDAO();
       /* if (getArguments() != null) {
            createEventDAO = getArguments().getParcelable("eventDAO");
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_tickt, container, false);
        view = binding.getRoot();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView(){

        binding.backArrow.setOnClickListener(v->getActivity().onBackPressed());

        editTicketAdapter = new EditTicketAdapter(getChildFragmentManager(), getActivity());
        binding.homeViewPager.setAdapter(editTicketAdapter);
        editTicketAdapter.notifyDataSetChanged();
        binding.tabLayout.setupWithViewPager(binding.homeViewPager);
        binding.tabLayout.getTabAt(0).select();


        binding.homeViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.tabLayout.getTabAt(0).select();
                } else if (position == 1) {
                    binding.tabLayout.getTabAt(1).select();
                } else if (position == 2) {
                    binding.tabLayout.getTabAt(2).select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}