package com.ecommerce.service.impl;

import com.ecommerce.entity.Address;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.service.interfaces.AddressService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public Address createAddress(Address address) {
        // If this is the first address for the user, make it the default
        List<Address> userAddresses = addressRepository.findByUserId(address.getUser().getId());
        if (userAddresses.isEmpty()) {
            address.setIsDefault(true);
        }
        
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(Long addressId, Address address) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));
        
        // Update fields
        existingAddress.setStreetAddress(address.getStreetAddress());
        existingAddress.setCity(address.getCity());
        existingAddress.setState(address.getState());
        existingAddress.setCountry(address.getCountry());
        existingAddress.setZipCode(address.getZipCode());
        
        // If setting this address as default, ensure it's for the same user
        if (address.getIsDefault() && !address.getUser().getId().equals(existingAddress.getUser().getId())) {
            throw new IllegalArgumentException("Cannot change user ID for an address");
        }
        
        // If making this address the default, update other addresses for this user
        if (address.getIsDefault() && !existingAddress.getIsDefault()) {
            setDefaultAddress(existingAddress.getUser().getId(), addressId);
        }
        
        return addressRepository.save(existingAddress);
    }

    @Override
    public Optional<Address> getAddressById(Long addressId) {
        return addressRepository.findById(addressId);
    }

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));
        
        addressRepository.deleteById(addressId);
        
        // If the deleted address was the default, set a new default if any other addresses exist
        if (address.getIsDefault()) {
            List<Address> remainingAddresses = addressRepository.findByUserId(address.getUser().getId());
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        // First, ensure the address exists and belongs to the user
        Address newDefaultAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));
        
        if (!newDefaultAddress.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("This address does not belong to the specified user");
        }
        
        // Remove default flag from all user addresses
        List<Address> userAddresses = addressRepository.findByUserId(userId);
        for (Address address : userAddresses) {
            if (address.getIsDefault()) {
                address.setIsDefault(false);
                addressRepository.save(address);
            }
        }
        
        // Set the new default address
        newDefaultAddress.setIsDefault(true);
        addressRepository.save(newDefaultAddress);
    }
}
