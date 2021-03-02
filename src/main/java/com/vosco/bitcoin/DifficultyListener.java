package com.vosco.bitcoin;
public interface DifficultyListener
{
    // will receive callback whenever the difficulty changes
    public void update(double percent);
}