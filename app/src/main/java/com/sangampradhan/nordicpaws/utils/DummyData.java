package com.sangampradhan.nordicpaws.utils;

import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.models.RoutineTask;

import java.util.ArrayList;
import java.util.List;

public class DummyData {

    public static List<Pet> getPets() {
        List<Pet> pets = new ArrayList<>();
        pets.add(new Pet("1", "Milo", "Golden Retriever • Male", "Up to date on vaccines", R.drawable.dog, "Dog", "2 yrs 4 mos", "Microchipped", "31.4 kg", "WELLNESS SCORE", "98% Optimal", R.drawable.baseline_health_and_safety_24));
        pets.add(new Pet("2", "Luna", "British Shorthair • Female", "Vet checkup due in 2 weeks", R.drawable.cat, "Cat", "1 yr 8 mos", "Spayed", "4.2 kg", "NEXT ROUTINE", "Grooming (Oct 18)", R.drawable.ic_event_note));
        pets.add(new Pet("3", "Oliver", "French Bulldog • Male", "Allergy Sensitive Plan", R.drawable.puppy, "Dog", "4 yrs", "Daily Mobility", "12.8 kg", "FEEDING", "Grain-free Duck", R.drawable.baseline_restaurant_24));
        return pets;
    }

    public static List<RoutineTask> getTasks() {
        List<RoutineTask> tasks = new ArrayList<>();
        // Milo's tasks
        tasks.add(new RoutineTask("t1", "1", "Morning Organic Kibble & Salmon Oil", "08:00 AM", "Feeding", true));
        tasks.add(new RoutineTask("t2", "1", "Probiotic & Joint Chews", "08:30 AM", "Medication", false));
        tasks.add(new RoutineTask("t3", "1", "Midday Park Walk & Fetch", "01:00 PM", "Exercise", false));
        tasks.add(new RoutineTask("t4", "1", "Coat Brushing & Dental Chew", "07:30 PM", "Grooming", false));

        // Luna's tasks
        tasks.add(new RoutineTask("t5", "2", "Morning Tuna Pate", "07:30 AM", "Feeding", true));
        tasks.add(new RoutineTask("t6", "2", "Laser Pointer Playtime", "11:00 AM", "Exercise", true));
        tasks.add(new RoutineTask("t7", "2", "Flea & Tick Prevention", "12:00 PM", "Healthcare", false));
        
        // Oliver's tasks
        tasks.add(new RoutineTask("t8", "3", "Morning Walk", "06:30 AM", "Exercise", true));
        tasks.add(new RoutineTask("t9", "3", "Dry Kibble Bowl", "07:00 AM", "Feeding", false));
        tasks.add(new RoutineTask("t10", "3", "Ear Cleaning", "04:00 PM", "Grooming", false));

        return tasks;
    }
    
    public static List<RoutineTask> getTasksForPet(String petId, String categoryFilter) {
        List<RoutineTask> allTasks = getTasks();
        List<RoutineTask> filtered = new ArrayList<>();
        for (RoutineTask t : allTasks) {
            if (t.getPetId().equals(petId)) {
                if (categoryFilter.equals("All") || t.getCategory().equals(categoryFilter)) {
                    filtered.add(t);
                }
            }
        }
        return filtered;
    }
}
