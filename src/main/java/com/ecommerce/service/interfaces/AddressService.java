package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Address;

import java.util.List;
import java.util.Optional;

public interface AddressService {
    Address createAddress(Address address);
    Address updateAddress(Long addressId, Address address);
    Optional<Address> getAddressById(Long addressId);
    List<Address> getAddressesByUserId(Long userId);
    void deleteAddress(Long addressId);
    void setDefaultAddress(Long userId, Long addressId);
}
