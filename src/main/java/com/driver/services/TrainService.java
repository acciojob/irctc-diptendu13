package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){
        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();

        // form the train route from the trainEntryDto and set it to the train
        String route = "";
        List<Station> stationList = trainEntryDto.getStationRoute();
        for (Station station : stationList){
            route += station + ",";
        }
        train.setRoute(route);

        // set the departure time of the train from the trainEntryDto
        train.setDepartureTime(trainEntryDto.getDepartureTime());

        // set the number of seats of the train from the trainEntryDto
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        // add train to trainRepository
        trainRepository.save(train);

        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        String[] routeList = train.getRoute().split(",");
        ArrayList<String> listOfStations = new ArrayList<>();
        Collections.addAll(listOfStations, routeList);

        int fromIndex = listOfStations.indexOf(seatAvailabilityEntryDto.getFromStation().toString());
        int toIndex = listOfStations.indexOf(seatAvailabilityEntryDto.getToStation().toString());

        int availableSeats = 0;
        for (Ticket ticket : train.getBookedTickets()){
            String fromStn = ticket.getFromStation().toString();
            String toStn = ticket.getToStation().toString();

            int idxFromStn = listOfStations.indexOf(fromStn);
            int idxToStn = listOfStations.indexOf(toStn);

            if ((idxFromStn <= fromIndex && idxToStn <= fromIndex) || (idxFromStn >= toIndex && idxToStn >= toIndex)){
                availableSeats += ticket.getPassengersList().size();
            }
        }
       return train.getNoOfSeats() - availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();

        // check if the given station lies in the route of the given train
        String[] routeList = train.getRoute().split(",");
        ArrayList<String> listOfStations = new ArrayList<>();
        Collections.addAll(listOfStations, routeList);

        if (!listOfStations.contains(station.toString())){
            throw new Exception("Train is not passing from this station");
        }
        int peopleCount = 0;
        for (Ticket ticket : train.getBookedTickets()){
            if (ticket.getFromStation().equals(station)){
                peopleCount += ticket.getPassengersList().size();
            }
        }
        return peopleCount;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        int oldestPersonAge = 0;
        Train train = trainRepository.findById(trainId).get();
        for (Ticket ticket : train.getBookedTickets()){
            for (Passenger passenger : ticket.getPassengersList()){
                if (passenger.getAge() > oldestPersonAge){
                    oldestPersonAge = passenger.getAge();
                }
            }
        }
        return oldestPersonAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Integer> listOfTrains = new ArrayList<>();
        for (Train train : trainRepository.findAll()){

            // first form list of the designated stations on the route of the train
            String[] routeList = train.getRoute().split(",");
            ArrayList<String> listOfStations = new ArrayList<>();
            Collections.addAll(listOfStations, routeList);

            // find if the given station exists in the designated route of the train
            if (listOfStations.contains(station.toString())){

                // form the arriving time at the given station
                int indexOfGivenStation = listOfStations.indexOf(station.toString());
                LocalTime arrivingTimeAtStation = train.getDepartureTime().plusHours(indexOfGivenStation);

                // if the train starts from the given station,then check if its departure time is within the given start time and end time
                if (listOfStations.get(0).equals(station.toString()) && train.getDepartureTime().compareTo(startTime) >= 0 && train.getDepartureTime().compareTo(endTime) <= 0){
                    listOfTrains.add(train.getTrainId());
                }
                // else check if the arriving time at the given station is within the given start time and end time
                else if (arrivingTimeAtStation.compareTo(startTime) >= 0 && arrivingTimeAtStation.compareTo(endTime) <= 0){
                    listOfTrains.add(train.getTrainId());
                }
            }
        }
        return listOfTrains;
    }

}
