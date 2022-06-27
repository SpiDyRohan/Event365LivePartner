package com.ebabu.event365live.host.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.FragmentEventCategoryBinding;
import com.ebabu.event365live.host.entities.Category;
import com.ebabu.event365live.host.entities.CategoryResponse;
import com.ebabu.event365live.host.entities.CreateEventDAO;
import com.ebabu.event365live.host.entities.SubCategory;
import com.ebabu.event365live.host.entities.SubCategoryResponse;
import com.ebabu.event365live.host.utils.Dialogs;
import com.ebabu.event365live.host.utils.MyLoader;
import com.ebabu.event365live.host.viewmodels.CreateEventViewModel;
import com.google.android.material.chip.Chip;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

public class EventCategoryFragment extends Fragment {

  private FragmentEventCategoryBinding binding;
  private  List<String> codes=new ArrayList<>();
  private CreateEventViewModel viewModel;
  private  List<Category> categoryList;
  private  List<SubCategory> subCategoryList;
  private MyLoader loader;
  private  List<SubCategory> selectedSubCategory=new ArrayList<>();
  private  int selectedCategory=-1;
    List<String> previousSubCats= new ArrayList();
    CreateEventDAO eventDAO;
    private String catIDSubID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_event_category,container,false);
        viewModel= ViewModelProviders.of(this).get(CreateEventViewModel.class);
        eventDAO= EventCategoryFragmentArgs.fromBundle(getArguments()).getEventDao();

        if(eventDAO.getSubCategoryId()!=null){
            previousSubCats.clear();
            String[] sArr =eventDAO.getSubCategoryId().substring(1,eventDAO.getSubCategoryId().length()-1).split(",");
            previousSubCats.addAll(Arrays.asList(sArr));
        }

        loader=new MyLoader(getContext());
        loader.show("");

        Observer<SubCategoryResponse> subCategoryObserver=new Observer<SubCategoryResponse>() {
            @Override
            public void onChanged(SubCategoryResponse subCategoryResponse) {
                if(subCategoryResponse.isSuccess()){
                    subCategoryList=subCategoryResponse.getCategories();
                    binding.subCategoryCg.removeAllViews();
                    selectedSubCategory.clear();

                    for(SubCategory subCategory:subCategoryList){
                        Chip chip = new Chip(getContext());
                        chip.setCheckable(true);
                        chip.setCheckedIconVisible(true);
                        chip.setText(subCategory.getSubCategoryName());
                        chip.setOnCheckedChangeListener((compoundButton, b)->{
                            if(b) {
                                if(selectedSubCategory.size()==5) {
                                    chip.setChecked(false);
                                    Dialogs.toast(getContext(), chip, getContext().getString(R.string.subCategoryLimitErr));
                                }
                                else
                                selectedSubCategory.add(subCategoryList.get(subCategoryList.indexOf(new SubCategory(chip.getText().toString()))));
                            }
                            else
                                selectedSubCategory.remove(new SubCategory(chip.getText().toString()));

                        });
                        if(previousSubCats.contains(String.valueOf(subCategory.getId())))
                            chip.setChecked(true);
                        binding.subCategoryCg.addView(chip);
                    }
                }
            }
        };



        viewModel.fetchCategories().observe(getViewLifecycleOwner(), new Observer<CategoryResponse>() {
            @Override
            public void onChanged(CategoryResponse categoryResponse) {
                loader.dismiss();
                int selectedIndex=-1,counter=0;
                if(categoryResponse.isSuccess()){
                    codes.clear();
                    categoryList=categoryResponse.getCategories();
                    for(Category category:categoryList){
                        if(category.getId()==eventDAO.getCategoryId())selectedIndex=counter;
                        codes.add(category.getCategoryName());
                        counter++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(container.getContext(),android.R.layout.simple_spinner_item,codes);
                    adapter.setDropDownViewResource(R.layout.spinner_layout);
                    binding.spinner.setAdapter(adapter);

                    if(counter>0)
                        binding.spinner.setSelection(selectedIndex);

                    binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            selectedCategory=categoryList.get(i).getId();
                            catIDSubID=categoryList.get(i).getCategoryName();
                            viewModel.fetchSubCategories(categoryList.get(i).getId()).observe(getViewLifecycleOwner(),subCategoryObserver);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                }
            }
        });

        binding.nextBtn.setOnClickListener(v->{

            if(selectedSubCategory.size()==0){
                Dialogs.toast(getActivity(),v,"Select a Sub Category please!");
                return;
            }

            eventDAO.setCategoryId(selectedCategory);
            catIDSubID+=" - ";
            JsonArray jsonArray=new JsonArray(selectedSubCategory.size());
            for(SubCategory category:selectedSubCategory) {
                jsonArray.add(category.getId());
                catIDSubID=catIDSubID.concat(category.getSubCategoryName()+", ");
            }

            eventDAO.setSubCategoryId(jsonArray.toString());
            eventDAO.setCatIDSubID(catIDSubID);
            Navigation.findNavController(v).navigate(
                    EventCategoryFragmentDirections.actionEventCategoryFragmentToCreateEventFragment().setEventDAO(eventDAO));
        });

        binding.backArrow.setOnClickListener(v->getActivity().onBackPressed());


        return binding.getRoot();
    }


}
