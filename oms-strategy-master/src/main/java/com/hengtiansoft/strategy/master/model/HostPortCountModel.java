package com.hengtiansoft.strategy.master.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostPortCountModel implements Comparable<HostPortCountModel> {
    private String hostPort;
    private int num;

    @Override
    public int compareTo(HostPortCountModel o) {
        if(o==null) {
            return 1;
        }
        return Integer.compare(num, o.num);
    }
}
