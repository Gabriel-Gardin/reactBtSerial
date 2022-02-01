import React from 'react';

import {ActivityIndicator} from 'react-native';

import {Container} from './styles';


function Loading() {
    return (
      <Container>
        <ActivityIndicator size="large" color='#dd7b22' />
      </Container>
    );
  };

export default Loading;