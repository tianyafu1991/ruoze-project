package com.ruozedata.ruozeplatformweb.controller;

import com.ruozedata.ruozeplatformweb.domain.MemberSex;
import com.ruozedata.ruozeplatformweb.domain.Result;
import com.ruozedata.ruozeplatformweb.repository.MemberSexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class StatController {

    public static Map<String,String > sexMap = new HashMap<>();
    static {
        sexMap.put("1","男");
        sexMap.put("2","女");
        sexMap.put("0","未知");
    }

    @Autowired
    private MemberSexRepository memberSexRepository;


    //http://localhost:9527/recommend/sex
    @GetMapping(value = "/sex")
    public List<Result> sex(){
        List<MemberSex> memberSexes = memberSexRepository.findAll();
        List<Result> results = new ArrayList<>();
        for (MemberSex memberSex : memberSexes) {
            results.add(new Result(sexMap.get(memberSex.getSex()),memberSex.getCnts()));
        }
        return results;

    }


}
