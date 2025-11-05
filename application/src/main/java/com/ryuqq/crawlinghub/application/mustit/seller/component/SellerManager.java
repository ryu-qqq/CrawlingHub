package com.ryuqq.crawlinghub.application.mustit.seller.component;

import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;

import org.springframework.stereotype.Component;

@Component
public class SellerManager {

    private final SaveSellerPort saveSellePort;
    private final LoadSellerPort loadSellerPort;

    public SellerManager(SaveSellerPort saveSellePort, LoadSellerPort loadSellerPort) {
        this.saveSellePort = saveSellePort;
        this.loadSellerPort = loadSellerPort;
    }

    public MustitSeller registerSeller(MustitSeller seller) {
        return saveSellePort.save(seller);
    }

    public MustitSeller updateSeller(Long sellerId) {
        return loadSellerPort.load(sellerId);
    }


}
