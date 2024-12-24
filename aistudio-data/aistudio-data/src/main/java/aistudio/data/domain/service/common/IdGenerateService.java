package aistudio.data.domain.service.common;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

@Service
public class IdGenerateService {

    public String generateId() {
        // Use a faster, but non-secure, random generator
        Random random = new Random();

        // Use a custom alphabet containing only a, b, and c
        char[] alphabet = {
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z'
        };

        // Make IDs 12 characters long
        int size = 12;

        return NanoIdUtils.randomNanoId(random, alphabet, size);
    }

}
