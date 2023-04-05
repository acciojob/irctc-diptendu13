package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        // check passenger id validity
//        if (!passengerRepository.existsById(bookTicketEntryDto.getBookingPersonId())){
//            throw new Exception("Invalid Passenger Id");
//        }

        // check train id validity
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        // check availability of tickets
        int bookedSeats = 0;
        for(Ticket ticket : train.getBookedTickets()){
            bookedSeats += ticket.getPassengersList().size();
        }
        int availableSeats = train.getNoOfSeats() - bookedSeats;
        if (bookTicketEntryDto.getNoOfSeats() > availableSeats){
            throw new Exception("Less tickets are available");
        }

        // check station names validity on designated train route
        int fromIndex = -1, toIndex = -1;
        String[] routeList = train.getRoute().split(",");
        for (int i=0; i<routeList.length; i++){
            if (routeList[i].equals(bookTicketEntryDto.getFromStation())){
                fromIndex = i;
            }
            if (routeList[i].equals(bookTicketEntryDto.getToStation())){
                toIndex = i;
            }
        }
        if (fromIndex == -1 || toIndex == -1){
            throw new Exception("Invalid stations");
        }

        Ticket ticket = new Ticket();

        // make the list of passengers and then add to ticket
        List<Passenger> listOfPassengers = new ArrayList<>();
        for (Integer passengerId : bookTicketEntryDto.getPassengerIds()){
            listOfPassengers.add(passengerRepository.findById(passengerId).get());
        }
        ticket.setPassengersList(listOfPassengers);

        // set from and to stations
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        // calculate and set total fare
        int totalFare = Math.abs(toIndex - fromIndex) * 300;
        ticket.setTotalFare(totalFare);

        // set train
        ticket.setTrain(train);

        // add ticket to booked tickets in train object
        train.getBookedTickets().add(ticket);

        // add ticket to booking person's booking list
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);

        // save all changes
        ticketRepository.save(ticket);

       return ticket.getTicketId();
    }
}
