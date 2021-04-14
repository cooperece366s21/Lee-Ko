import React from 'react';
import User from './User';
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Link
} from "react-router-dom";

import {
    Box,
    Container,
    IconButton,
    Center,
    HStack,
    Text,
    Input,
} from "@chakra-ui/react";
import UserChakra from "./LoginChakra";

class HeaderChakra extends React.Component<any, any>{

    render() {
        return(
            <>
                <Box w={'100%'} position={'relative'} top={'0'} h={'90'} pt={'5px'} mb={'30px'} bg={'#fff463'}>

                    <HStack p={0} m={0}>

                        {/*Logo*/}
                        <Box w={'15%'} h={'100%'}>
                            <Center>
                                <Link to={'/'}>
                                    <Text fontSize={'5xl'} fontWeight={800}> G&D </Text>
                                </Link>
                            </Center>
                        </Box>

                        {/*Navigation*/}
                        <Box w={'35%'} pl={50}>
                            <Center>

                                <Link to={'/'}>
                                    <Text mr={20} fontWeight={500} fontSize={'2xl'}>
                                        Home
                                    </Text>
                                </Link>

                                <Link to={'/about'}>
                                    <Text mr={20} fontWeight={500} fontSize={'2xl'}>
                                        About
                                    </Text>
                                </Link>

                                <Link to={'/feed'}>
                                    <Text mr={20} fontWeight={500} fontSize={'2xl'}>
                                        Feed
                                    </Text>
                                </Link>

                            </Center>
                        </Box>

                        {/*User Search*/}
                        <Box w={'20%'}>
                            <Input placeholder={'Search for a user'} bg={'gray.50'}/>
                        </Box>

                        {/*User Component*/}
                        <Box w={'25%'}>
                            <UserChakra />
                        </Box>

                    </HStack>

                </Box>
            </>
        )

    }

}

export default HeaderChakra;