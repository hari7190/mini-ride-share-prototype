auth-service => /register
                /login

rider-service => /api/rider/request => MySQL
                                    => Kafka
                                            => dispatch-engine => MySQL
                                                               => Kafka
driver-service => 


location


