package soonmap.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soonmap.entity.Menu;
import soonmap.repository.MenuRepository;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public void save(Menu menu) {
        menuRepository.save(menu);
    }

}
